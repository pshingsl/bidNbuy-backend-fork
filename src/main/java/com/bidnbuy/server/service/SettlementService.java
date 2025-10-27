package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.SettlementEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.SettlementStatus;
import com.bidnbuy.server.repository.OrderRepository;
import com.bidnbuy.server.repository.SettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;

    // 정산금 전달(구매자 구매 완료? 상품 받았음등)
    @Transactional
    public void confirmSettlement(Long orderId, Long userId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 구매자 본인 확인
        if (!order.getBuyer().getUserId().equals(userId)) {
            throw new IllegalStateException("본인 주문만 확정할 수 있습니다.");
        }

        // Settlement 조회
        SettlementEntity settlement = settlementRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다."));

        if (settlement.getPayoutStatus() == SettlementStatus.DONE) {
            throw new IllegalStateException("이미 정산 완료된 주문입니다.");
        }

        UserEntity seller = settlement.getSeller();
        if (seller.getAccountNumber() == null || seller.getAccountNumber().isBlank()) {
            throw new IllegalStateException("판매자 계좌 정보가 등록되지 않았습니다.");
        }

        // 상태 변경
        settlement.setPayoutStatus(SettlementStatus.DONE);
        settlement.setPayoutAt(LocalDateTime.now());
        settlementRepository.save(settlement);

        // 주문 상태도 완료로 변경
        order.setOrderStatus("COMPLETED");
        orderRepository.save(order);
    }
}
