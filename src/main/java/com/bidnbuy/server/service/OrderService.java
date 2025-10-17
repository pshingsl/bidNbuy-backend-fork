package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.OrderRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    // 볍점 부여
    @Transactional
    public void rateOrder(Long orderId, Long buyerId, int rating) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        // 1) 구매자 본인 거래인지 확인
        if (order.getBuyer().getUserId() != buyerId) {
            throw new IllegalStateException("본인의 거래만 평가할 수 있습니다.");
        }

        // 2) 주문 상태 확인 (완료 상태만 가능)
        if (!"COMPLETED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("거래 완료 상태에서만 별점을 줄 수 있습니다.");
        }

        // 3) 이미 별점 등록된 경우 방지
        if (order.getRating() > 0) {
            throw new IllegalStateException("이미 별점이 등록된 거래입니다.");
        }

        // 4) 별점 유효성 체크
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("별점은 1~5 사이여야 합니다.");
        }

        // 저장
        order.setRating(rating);
        orderRepository.save(order);

        //  판매자 온도 갱신
        Double avg = orderRepository.getAverageRatingBySeller(order.getSeller().getUserId());
        Double newTemperature = (avg != null ? avg * 10 : null);

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

        // (reason은 로그용으로만 사용하거나, 별도 테이블에 기록 가능)
        return new OrderUpdateResponseDto(order.getOrderId(), "주문 상태가 변경되었습니다.");
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
            // CASE A: 아직 결제 안 됨 → 주문만 취소
            if (order.getPayment() == null) {
                order.setOrderStatus("CANCELED");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                continue;
            }

            // CASE B: 결제 완료된 주문 → Toss 취소 + 로그 남기기 + 주문 취소
            try {
                Integer cancelAmount = order.getPayment().getTotalAmount();

                // ✅ paymentService의 cancelPayment() 재사용 (일반 취소 로직 그대로 활용)
                PaymentCancelRequestDto dto = new PaymentCancelRequestDto(
                        order.getPayment().getTossPaymentKey(),
                        "결제 기한 초과 자동 취소",
                        cancelAmount
                );
                paymentService.cancelPayment(dto);

                // ✅ 주문 취소 상태 반영
                order.setOrderStatus("CANCELED");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

            } catch (Exception e) {
                // 예외 발생 시 로그만 남기고 넘어가기 (스케줄 전체 멈추지 않게)
                System.err.println("자동 취소 실패 (orderId=" + order.getOrderId() + "): " + e.getMessage());
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

        OrderEntity order = new OrderEntity();
        order.setSeller(seller);
        order.setBuyer(buyer);
        order.setType(dto.getType());
        order.setOrderStatus("PENDING"); // 초기 상태
        order.setRating(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        OrderEntity saved = orderRepository.save(order);

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
