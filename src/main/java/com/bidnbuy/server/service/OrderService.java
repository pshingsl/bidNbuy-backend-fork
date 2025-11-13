package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.NotificationType;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final AuctionResultRepository auctionResultRepository;
    private final ChatMessageService chatMessageService;
    private final ChatRoomRepository chatRoomRepository;
    private final AuctionProductsRepository auctionProductsRepository;
    private final UserNotificationService notificationService;
    private final AddressRepository addressRepository;

    // 볍점 부여
    @Transactional
    public void rateOrder(Long orderId, Long buyerId, int rating) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 1) 구매자 본인 거래인지 확인
        if (order.getBuyer().getUserId() != buyerId) {
            throw new IllegalStateException("본인의 거래만 평가할 수 있습니다.");
        }

        // 2) 주문 상태 확인 (결제 상태만 가능)
        if (!"PAID".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("결제 완료 상태에서만 별점을 줄 수 있습니다.");
        }

        // 3) 이미 별점 등록된 경우 방지
        if (order.getRating() > 0) {
            throw new IllegalStateException("이미 별점이 등록된 거래입니다.");
        }

        // 4) 별점 유효성 체크
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("별점은 1~10 사이여야 합니다.");
        }

        // 저장
        order.setRating(rating);
        orderRepository.save(order);

        //  판매자 온도 갱신
        Double avg = orderRepository.getAverageRatingBySeller(order.getSeller().getUserId());
        Double newTemperature = (avg != null ? avg * 10 : 0);

        UserEntity seller = order.getSeller();
        seller.setUserTemperature(newTemperature);
        userRepository.save(seller);
    }


    //주문 상태 업데이트
    @Transactional
    public OrderUpdateResponseDto updateOrderStatus(Long orderId, Long userId, OrderUpdateRequestDto dto) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. id=" + orderId));

        // 권한 체크 (해당 주문의 판매자 또는 구매자만 수정 가능)
        if (!Objects.equals(order.getBuyer().getUserId(), userId) &&
                !Objects.equals(order.getSeller().getUserId(), userId)) {
            throw new SecurityException("해당 주문을 변경할 권한이 없습니다.");
        }

        // 상태 업데이트
        order.setOrderStatus(dto.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 최종 완료 상태일 경우 AuctionResultEntity 상태도 변경
        if ("COMPLETED".equalsIgnoreCase(dto.getStatus())) {
            // 낙찰자의 정보를 가져온다.

            List<AuctionResultEntity> results = auctionResultRepository.findByOrder_OrderId(orderId);

            if (!results.isEmpty()) {
                AuctionResultEntity result = results.get(0);

                result.setResultStatus(ResultStatus.SUCCESS_COMPLETED); // 최종 거래 완료 상태
                auctionResultRepository.save(result);

                System.out.println("거래 완료 상태로 변경 완료: AuctionResult ID " + result.getResultId());
            }

            Long chatroomId = findChatRoomIdForOrder(order);

            String autoMessage = String.format(
                    //자동 메세지 고정 내용
                    "결제가 완료되었습니다. 주문번호 : %d 거래가 성공적으로 마무리되었습니다.", orderId
            );
            chatMessageService.sendAutoMessage(chatroomId, autoMessage);
        }

        // (reason은 로그용으로만 사용하거나, 별도 테이블에 기록 가능)
        return new OrderUpdateResponseDto(order.getOrderId(), "주문 상태가 변경되었습니다.");
    }

    //채팅방 아이디 찾기
    private Long findChatRoomIdForOrder(OrderEntity order) {
        Long buyerId = order.getBuyer().getUserId();
        Long sellerId = order.getSeller().getUserId();

        //경매 아이디 추출
        AuctionResultEntity result = order.getResult();
        if (result == null || result.getAuction() == null) {
            throw new IllegalArgumentException("주문 id " + order.getOrderId() + "에 경매결과 누락");
        }
        Long auctionProductId = result.getAuction().getAuctionId();

        ChatRoomEntity chatRoom = chatRoomRepository
                .findByBuyerId_UserIdAndSellerId_UserIdAndAuctionId_AuctionId(
                        buyerId,
                        sellerId,
                        auctionProductId
                ).orElseThrow(() -> new EntityNotFoundException("주문 id" + order.getOrderId() + "와 관련된 채팅방을 찾을 수 없음"));
        return chatRoom.getChatroomId();
    }


    //상세 조회
    @Transactional
    public OrderResponseDto getOrderDetail(Long orderId, Long userId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // 📝 권한 체크: 내가 구매자 or 판매자일 때만 조회 가능
        if (!Objects.equals(order.getBuyer().getUserId(), userId) &&
                !Objects.equals(order.getSeller().getUserId(), userId)) {
            throw new SecurityException("해당 주문을 조회할 권한이 없습니다.");
        }

        return OrderResponseDto.builder()
                .orderId(order.getOrderId())
                .sellerId(order.getSeller().getUserId())
                .buyerId(order.getBuyer().getUserId())
                .type(order.getType())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


    // 조회
    public List<OrderResponseDto> getMyOrders(Long userId, String type, String status) {
        List<OrderEntity> orders;

        if ("PURCHASE".equalsIgnoreCase(type)) {
            orders = orderRepository.findPurchaseOrders(userId, status);
        } else if ("SALE".equalsIgnoreCase(type)) {
            orders = orderRepository.findSaleOrders(userId, status);
        } else {
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        return orders.stream()
                .map(o -> OrderResponseDto.builder()
                        .orderId(o.getOrderId())
                        .sellerId(o.getSeller().getUserId())
                        .buyerId(o.getBuyer().getUserId())
                        .type(o.getType())
                        .orderStatus(o.getOrderStatus())
                        .createdAt(o.getCreatedAt())
                        .updatedAt(o.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }


    /**
     * 자동 취소 로직 (스케줄러에서 주기적으로 호출)
     * - CASE A: 아직 결제 진행 안됨 → 주문만 취소
     * - CASE B: 결제 완료된 주문 → Toss 취소 + 로그 남기기 + 주문 취소
     */
    @Transactional
    public void autoCancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24); // 낙찰 후 24시간 기준
        List<OrderEntity> expiredOrders = orderRepository.findExpiredOrders(deadline);

        for (OrderEntity order : expiredOrders) {
            // CASE A: 아직 결제 안 됨 (PENDING)
            if ("PENDING".equalsIgnoreCase(order.getOrderStatus())) {
                order.setOrderStatus("CANCELED");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                try {
                    // 자동 취소 알림 추가
                    String cancelMessage = String.format(
                            "결제 기한이 만료되어 주문이 자동 취소되었습니다. (주문번호: %d)",
                            order.getOrderId()
                    );

                    Long buyerId = order.getBuyer().getUserId();

                    notificationService.createNotificationforChat(
                            buyerId,
                            NotificationType.ALERT,   // 일반 알림 타입
                            cancelMessage,
                            order.getResult() != null ? order.getResult().getAuction().getAuctionId() : null,
                            order.getSeller().getUserId()
                    );

                    log.info("🕒 [자동취소] PENDING 주문 자동취소 및 알림 전송 완료 - orderId={}", order.getOrderId());
                } catch (Exception e) {
                    log.error("⚠️ [자동취소] PENDING 주문 알림 실패 - orderId={}, error={}", order.getOrderId(), e.getMessage());
                }

                continue;
            }

            // CASE B: 결제 완료된 주문 (PAID)
            if ("PAID".equalsIgnoreCase(order.getOrderStatus())) {
                try {
                    Integer cancelAmount = order.getPayment().getTotalAmount();

                    PaymentCancelRequestDto dto = new PaymentCancelRequestDto(
                            order.getPayment().getTossPaymentKey(),
                            "결제 기한 초과 자동 취소",
                            cancelAmount
                    );
                    paymentService.cancelPayment(dto);

                    order.setOrderStatus("CANCELED");
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);

                    // 결제된 주문도 알림 전송 추가
                    String paidCancelMessage = String.format(
                            "결제 완료된 주문이 기한 초과로 자동 취소되었습니다. (주문번호: %d)",
                            order.getOrderId()
                    );

                    notificationService.createNotificationforChat(
                            order.getBuyer().getUserId(),
                            NotificationType.ALERT,
                            paidCancelMessage,
                            order.getResult() != null ? order.getResult().getAuction().getAuctionId() : null,
                            order.getSeller().getUserId()
                    );

                    log.info("💳 [자동취소] PAID 주문 자동취소 및 알림 전송 완료 - orderId={}", order.getOrderId());
                } catch (Exception e) {
                    log.error("자동 취소 실패 (orderId={}): {}", order.getOrderId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 신규 주문 저장
     */
    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto dto) {
        UserEntity seller = userRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + dto.getSellerId()));

        UserEntity buyer = userRepository.findById(dto.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + dto.getBuyerId()));

        // ⭐ auctionId → AuctionProductsEntity 변환
        AuctionProductsEntity auction = auctionProductsRepository.findById(dto.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + dto.getAuctionId()));

        // 주소추가
        AddressEntity address;

        if (dto.getAddressId() != null) {
            address = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("선택한 배송지를 찾을 수 없습니다: " + dto.getAddressId()));

            if (!address.getUser().getUserId().equals(buyer.getUserId())) {
                throw new IllegalArgumentException("선택한 배송지는 해당 구매자의 배송지가 아닙니다.");
            }

        } else {
            address = addressRepository
                    .findFirstByUser_UserIdOrderByCreatedAtDesc(buyer.getUserId())
                    .orElse(null);
        }


        // 1. 이미 같은 경매/구매자 조합의 주문이 존재하는지 확인
        OrderEntity existing = orderRepository
                .findFirstByBuyer_UserIdAndResult_Auction_AuctionId(dto.getBuyerId(), dto.getAuctionId())
                .orElse(null);

        if (existing != null) {
            log.info("⚠️ 기존 주문 존재 → orderId={} 그대로 반환", existing.getOrderId());
            return OrderResponseDto.builder()
                    .orderId(existing.getOrderId())
                    .sellerId(existing.getSeller().getUserId())
                    .buyerId(existing.getBuyer().getUserId())
                    .type(existing.getType())
                    .orderStatus(existing.getOrderStatus())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(existing.getUpdatedAt())
                    .build();
        }

        OrderEntity order = new OrderEntity();
        order.setSeller(seller);
        order.setBuyer(buyer);
        order.setType(dto.getType());
        order.setOrderStatus("PENDING"); // 초기 상태
        order.setRating(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setShippingAddress(address);

        // 2. 기존 auction에 대한 result 존재 여부 체크
        AuctionResultEntity existingResult = auctionResultRepository
                .findFirstByAuction_AuctionId(dto.getAuctionId())
                .orElse(null);

        // result 직접 생성
        AuctionResultEntity result = AuctionResultEntity.builder()
                .auction(auction)
                .winner(buyer) // 구매자 == 낙찰자
                .order(order)
                .resultStatus(ResultStatus.SUCCESS_PENDING_PAYMENT) // 기본 상태
                .finalPrice(auction.getCurrentPrice())
                .closedAt(LocalDateTime.now())
                .build();


        order.setResult(result);


        // 저장
        OrderEntity saved = orderRepository.save(order);

        // 중복위험
        auctionResultRepository.save(result);


        return OrderResponseDto.builder()
                .orderId(saved.getOrderId())
                .sellerId(saved.getSeller().getUserId())
                .buyerId(saved.getBuyer().getUserId())
                .type(saved.getType())
                .orderStatus(saved.getOrderStatus())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();

    }

    public OrderEntity findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

}
