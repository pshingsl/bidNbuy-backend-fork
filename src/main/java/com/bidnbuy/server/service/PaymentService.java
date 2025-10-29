package com.bidnbuy.server.service;

import com.bidnbuy.server.config.TossPaymentClient;
import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.AuctionStatus;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SellingStatus;
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
    private final AuctionProductsRepository auctionProductsRepository;
    private final AuctionHistoryRepository auctionHistoryRepository;
    private final SettlementService settlementService;


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

        // 결제진행 완료
        if (newStatus == paymentStatus.PaymentStatus.SUCCESS) {
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
            // 주문 상태 갱신
            order.setOrderStatus("PAID");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // 경매 결과 조회
            List<AuctionResultEntity> results = auctionResultRepository.findByOrder_OrderId(orderId);

            // 있으면 상태만 변경 없으면 결과 생성
            if (!results.isEmpty()) {
                // 기존 결과 업데이트
                AuctionResultEntity result = results.get(0);
                result.setResultStatus(ResultStatus.SUCCESS_PAID);
                auctionResultRepository.save(result);
            } else {
                // 새로운 결과 생성
                Long auctionId = dto.getAuctionId();  // 프론트에서 받아온 auctionId
                if (auctionId == null) {
                    throw new IllegalArgumentException("결제 승인에 필요한 auctionId가 없습니다.");
                }

                AuctionProductsEntity auction = auctionProductsRepository.findById(auctionId)
                        .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다. auctionId=" + auctionId));

// history 생성
                AuctionHistoryEntity history = AuctionHistoryEntity.builder()
                        .auctionProduct(auction)
                        .previousStatus(AuctionStatus.PAYMENT_PENDING) // 기존 상태
                        .newStatus(AuctionStatus.PAYMENT_COMPLETED) // 결제 완료
                        .bidTime(LocalDateTime.now())
                        .build();

                auctionHistoryRepository.save(history);

// result 생성
                AuctionResultEntity result = AuctionResultEntity.builder()
                        .auction(auction)
                        .winner(order.getBuyer())
                        .resultStatus(ResultStatus.SUCCESS_PAID) // ResultStatus는 SUCCESS_PAID
                        .finalPrice(savedPayment.getTotalAmount())
                        .history(history) // 방금 만든 history 연결
                        .order(order)
                        .closedAt(LocalDateTime.now())
                        .build();

                auctionResultRepository.save(result);

                // Order와 Result 연결
                order.setResult(result);
                orderRepository.save(order);
                
                // Settlement 생성 (정산 정보)
                settlementService.createSettlement(order, savedPayment.getTotalAmount());
            }
        }

        // 2. Toss 응답 상태가 COMPLETED일 경우 (최종 거래 완료 상태)
        if (newStatus == paymentStatus.PaymentStatus.SUCCESS && "COMPLETED".equalsIgnoreCase(dto.getStatus())) {

            // order 상태 변경
            OrderEntity order = savedPayment.getOrder();
            if (order != null) {
                order.setOrderStatus("COMPLETED");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }

            // auctionResult 상태 변경 or 생성
            List<AuctionResultEntity> results =
                    auctionResultRepository.findByOrder_OrderId(savedPayment.getOrder().getOrderId());

            if (!results.isEmpty()) {
                AuctionResultEntity result = results.get(0);
                result.setResultStatus(ResultStatus.SUCCESS_COMPLETED); // 최종 거래 완료 상태
                auctionResultRepository.save(result);
            } else {
                // 새로운 결과 생성
                Long auctionId = dto.getAuctionId();  // 프론트에서 받아온 auctionId
                if (auctionId == null) {
                    throw new IllegalArgumentException("결제 승인에 필요한 auctionId가 없습니다.");
                }

                AuctionProductsEntity auction = auctionProductsRepository.findById(auctionId)
                        .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다. auctionId=" + auctionId));

                // 1) 결제 성공 시 (PAID)

// history 생성
                AuctionHistoryEntity history = AuctionHistoryEntity.builder()
                        .auctionProduct(auction)
                        .previousStatus(AuctionStatus.PAYMENT_PENDING) // 기존 상태
                        .newStatus(AuctionStatus.PAYMENT_COMPLETED) // 결제 완료
                        .bidTime(LocalDateTime.now())
                        .build();

                auctionHistoryRepository.save(history);

// result 생성
                AuctionResultEntity result = AuctionResultEntity.builder()
                        .auction(auction)
                        .winner(order.getBuyer())
                        .resultStatus(ResultStatus.SUCCESS_PAID) // ResultStatus는 SUCCESS_PAID
                        .finalPrice(savedPayment.getTotalAmount())
                        .history(history) // 방금 만든 history 연결
                        .order(order)
                        .closedAt(LocalDateTime.now())
                        .build();

                auctionResultRepository.save(result);

                // Order와 Result 연결
                order.setResult(result);
                orderRepository.save(order);
            }

            // 정산처리 나중에
        }

        // 3. 결제 취소/실패 처리
        if (newStatus == paymentStatus.PaymentStatus.FAIL || newStatus == paymentStatus.PaymentStatus.CANCEL) {
            OrderEntity order = savedPayment.getOrder();
            if (order != null) {
                order.setOrderStatus("CANCELLED");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }

            PaymentCancelEntity cancelEntity = new PaymentCancelEntity();
            cancelEntity.setPayment(savedPayment);
            cancelEntity.setTransactionKey(dto.getPaymentKey()); // Toss 결제키를 트랜잭션키로 저장
            cancelEntity.setCancelReason("승인 응답에서 취소/실패 처리"); // 필요 시 dto에서 사유 가져오기
            cancelEntity.setCancelAmount(savedPayment.getTotalAmount()); // 전액 취소 기준
            cancelEntity.setCancelStatus(dto.getStatus()); // Toss 응답 상태 그대로
            cancelEntity.setCanceledAt(LocalDateTime.now()); // Toss 응답 canceledAt 있으면 파싱해서 넣기
            cancelEntity.setCreatedAt(LocalDateTime.now());

            paymentCancelRepository.save(cancelEntity);

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
