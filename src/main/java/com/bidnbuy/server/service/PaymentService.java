package com.bidnbuy.server.service;

import com.bidnbuy.server.config.TossPaymentClient;
import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SettlementStatus;
import com.bidnbuy.server.enums.paymentStatus;
import com.bidnbuy.server.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCancelRepository paymentCancelRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final AuctionResultRepository auctionResultRepository;
    private final SettlementRepository settlementRepository;


    // 자동 취소 (스케줄러가 호출)
    @Transactional
    public void autoCancelExpiredOrders(List<OrderEntity> expiredOrders) {
        for (OrderEntity order : expiredOrders) {
            try {
                if (order.getPayment() != null) {
                    // 기존 cancelPayment 로직 그대로 사용
                    PaymentCancelRequestDto dto = new PaymentCancelRequestDto(
                            order.getPayment().getTossPaymentKey(),
                            "결제 기한 초과 자동 취소",
                            order.getPayment().getTotalAmount()
                    );
                    cancelPayment(dto); // <-- 기존 로직 재활용
                }

                // 주문 상태 변경
                order.setOrderStatus("CANCELED");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

            } catch (Exception e) {
                log.error("자동 취소 실패 (orderId={}): {}", order.getOrderId(), e.getMessage());
            }
        }
    }


    // 취소
    public PaymentCancelResponseDto cancelPayment(PaymentCancelRequestDto requestDto) {
        // 1. DB에서 결제 찾기
        List<PaymentEntity> payments = paymentRepository
                .findByTossPaymentKeyAndTossPaymentKeyIsNotNull(requestDto.getPaymentKey());

        if (payments.isEmpty()) {
            throw new IllegalArgumentException("해당 결제를 찾을 수 없습니다.");
        }

        if (payments.size() > 1) {
            throw new IllegalStateException("결제키 중복 오류 발생: " + requestDto.getPaymentKey());
        }

        PaymentEntity payment = payments.get(0);

        try {
            // 3. Toss API 호출
            HttpResponse<String> response = tossPaymentClient.cancelPayment(
                    requestDto.getPaymentKey(),
                    requestDto.getCancelReason(),
                    requestDto.getCancelAmount()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("Toss 취소 요청 실패: " + response.body());
            }

            // 4. Toss 응답 파싱
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode cancelNode = json.get("cancels").get(0); // 첫번째 취소 내역

            // 5. 취소 로그 엔티티 생성
            PaymentCancelEntity cancelEntity = new PaymentCancelEntity();
            cancelEntity.setPayment(payment);
            cancelEntity.setTransactionKey(cancelNode.get("transactionKey").asText());
            cancelEntity.setCancelReason(cancelNode.get("cancelReason").asText());
            cancelEntity.setCancelAmount(cancelNode.get("cancelAmount").asInt());
            cancelEntity.setCancelStatus(cancelNode.get("cancelStatus").asText());
            cancelEntity.setReceiptKey(cancelNode.hasNonNull("receiptKey") ? cancelNode.get("receiptKey").asText() : null);
            cancelEntity.setCanceledAt(LocalDateTime.parse(cancelNode.get("canceledAt").asText(),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            cancelEntity.setCreatedAt(LocalDateTime.now());

            // 6. DB 반영
            payment.setTossPaymentStatus(paymentStatus.PaymentStatus.CANCEL);
            paymentCancelRepository.save(cancelEntity);
            paymentRepository.save(payment);

            // 7. 응답 DTO 리턴
            return new PaymentCancelResponseDto(
                    payment.getTossPaymentKey(),
                    cancelEntity.getTransactionKey(),
                    cancelEntity.getCancelReason(),
                    cancelEntity.getCancelAmount(),
                    cancelEntity.getCancelStatus(),
                    cancelEntity.getReceiptKey(),
                    cancelEntity.getCanceledAt()
            );

        } catch (Exception e) {
            throw new RuntimeException("결제 취소 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }




    /**
     * merchantOrderId로 Payment 조회
     */
    public Optional<PaymentEntity> findByMerchantOrderId(String merchantOrderId) {
        return paymentRepository.findByMerchantOrderId(merchantOrderId);
    }

    /**
     * PENDING 상태의 Payment 생성
     */
    @Transactional
    public PaymentEntity createPendingPayment(OrderEntity order, SaveAmountRequest request) {
        // 이미 merchantOrderId 가 존재하면 그대로 리턴
        Optional<PaymentEntity> existing = paymentRepository.findByMerchantOrderId(request.getMerchantOrderId());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 없으면 새로 생성
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setMerchantOrderId(request.getMerchantOrderId());
        payment.setTotalAmount(request.getAmount());
        payment.setTossPaymentStatus(paymentStatus.PaymentStatus.PENDING);
        payment.setRequestedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    /**
     * Toss 결제 승인 응답으로 Payment 갱신
     */
    @Transactional
    public PaymentEntity saveConfirmedPayment(PaymentResponseDto dto) {
        PaymentEntity payment = paymentRepository.findByMerchantOrderId(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found by merchantOrderId: " + dto.getOrderId()));

        // 이미 성공 상태라면 그대로 리턴
        if (payment.getTossPaymentStatus() == paymentStatus.PaymentStatus.SUCCESS) {
            log.info("이미 승인된 결제: {}", dto.getOrderId());
            return payment;
        }

        payment.setTossPaymentKey(dto.getPaymentKey());
        payment.setTossPaymentStatus(mapToPaymentStatus(dto.getStatus()));
        payment.setTossPaymentMethod(mapToPaymentMethod(dto.getMethod()));

        paymentStatus.PaymentStatus newStatus = mapToPaymentStatus(dto.getStatus());
        payment.setTossPaymentStatus(newStatus);

        if (dto.getApprovedAt() != null) {
            OffsetDateTime odt = OffsetDateTime.parse(dto.getApprovedAt());
            payment.setApprovedAt(odt.toLocalDateTime());
        }

        PaymentEntity savedPayment = paymentRepository.save(payment);

        if(newStatus == paymentStatus.PaymentStatus.SUCCESS) {
            OrderEntity order = savedPayment.getOrder();
            if (order == null) {
                throw new IllegalStateException("Payment에 연결된 Order를 찾을 수 없습니다.");
            }
            Long orderId = order.getOrderId();
            if ("PAID".equalsIgnoreCase(order.getOrderStatus()) ||
                    "COMPLETED".equalsIgnoreCase(order.getOrderStatus())) {
                System.err.println("경고: 이미 결제 완료된 주문을 다시 PAID로 시도함. Order ID: " + orderId);
                return savedPayment;
            }
            order.setOrderStatus("PAID");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            System.out.println("주문 상태 PAID로 변경 완료: Order ID " + orderId);

            // 정산 생성
            SettlementEntity settlement = SettlementEntity.builder()
                    .order(order)
                    .seller(order.getSeller())
                    .payoutAmount(savedPayment.getTotalAmount())
                    .payoutStatus(SettlementStatus.WAITING)
                    .build();
            settlementRepository.save(settlement);


            // 경매 결과 상태 변경
            List<AuctionResultEntity> results = auctionResultRepository.findByOrder_OrderId(orderId);

            if(!results.isEmpty()) {
                AuctionResultEntity result = results.get(0);

                result.setResultStatus(ResultStatus.SUCCESS_PAID);
                auctionResultRepository.save(result);

                System.out.println("경매 결과 상태 SUCCESS_PAID로 변경 완료: AuctionResult ID " + result.getResultId());
            }
        }

        // 4. Toss 응답 상태가 COMPLETED일 경우 (최종 거래 완료 상태)
        if ("COMPLETED".equalsIgnoreCase(dto.getStatus())) {

            List<AuctionResultEntity> results = auctionResultRepository.findByOrder_OrderId(savedPayment.getOrder().getOrderId());

            if (!results.isEmpty()) {
                AuctionResultEntity result = results.get(0);

                result.setResultStatus(ResultStatus.SUCCESS_COMPLETED); // 최종 거래 완료 상태
                auctionResultRepository.save(result);

                System.out.println("거래 완료 상태로 변경 완료: AuctionResult ID " + result.getResultId());
            }
        }
        return paymentRepository.save(payment);
    }

    /**
     * Toss status 문자열 → 내부 Enum 매핑
     */
    private paymentStatus.PaymentStatus mapToPaymentStatus(String tossStatus) {
        if (tossStatus == null) return paymentStatus.PaymentStatus.FAIL;

        switch (tossStatus) {
            case "DONE":
                return paymentStatus.PaymentStatus.SUCCESS;
            case "CANCELED":
            case "PARTIAL_CANCELED":
                return paymentStatus.PaymentStatus.CANCEL;
            case "ABORTED":
            case "EXPIRED":
                return paymentStatus.PaymentStatus.FAIL;
            case "REFUND":
                return paymentStatus.PaymentStatus.REFUND;
            default:
                return paymentStatus.PaymentStatus.FAIL; // 안전 fallback
        }
    }

    /**
     * Toss method 문자열 → 내부 Enum 매핑
     */
    private paymentStatus.PaymentMethod mapToPaymentMethod(String tossMethod) {
        if (tossMethod == null) return null;

        switch (tossMethod) {
            case "CARD":
            case "카드":
                return paymentStatus.PaymentMethod.CARD;
            case "VIRTUAL_ACCOUNT":
            case "가상계좌":
                return paymentStatus.PaymentMethod.VIRTUAL_ACCOUNT;
            case "TRANSFER":
            case "계좌이체":
                return paymentStatus.PaymentMethod.TRANSFER;
            case "MOBILE_PHONE":
            case "휴대폰":
                return paymentStatus.PaymentMethod.MOBILE;
            case "CULTURE_GIFT_CERTIFICATE":
            case "문화상품권":
                return paymentStatus.PaymentMethod.CULTURE_GIFT;
            case "BOOK_GIFT_CERTIFICATE":
            case "도서문화상품권":
                return paymentStatus.PaymentMethod.BOOK_GIFT;
            case "GAME_GIFT_CERTIFICATE":
            case "게임문화상품권":
                return paymentStatus.PaymentMethod.GAME_GIFT;
            case "SIMPLE_PAY":
            case "EASY_PAY":
            case "간편결제":
            case "TOSSPAY":
            case "PAYCO":
                return paymentStatus.PaymentMethod.SIMPLE_PAY;
            default:
                log.warn("⚠️ 매핑되지 않은 Toss method 들어옴: {}", tossMethod);
                return null; // 예외 던지지 말고 fallback
        }
    }
}
