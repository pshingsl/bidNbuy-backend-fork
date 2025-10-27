package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.SettlementEntity;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SettlementStatus;
import com.bidnbuy.server.repository.SettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;

    // 정산 등록(결제 완료 이후)
    @Transactional
    public void createSettlement(OrderEntity order, Integer payoutAmount) {
        SettlementEntity settlement = new SettlementEntity();
        settlement.setOrder(order);
        settlement.setPayoutAmount(payoutAmount);
        settlement.setPayoutStatus(SettlementStatus.WAITING); // 처음엔 대기
        settlement.setPayoutAt(null);

        settlementRepository.save(settlement);
    }

    // 정산 완료(DONE)
    @Transactional
    public void confirmSettlement(Long orderId, Long userId) {
        SettlementEntity settlement = settlementRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다."));

        OrderEntity order = settlement.getOrder();
        if (order == null) {
            throw new IllegalStateException("정산에 연결된 주문이 없습니다.");
        }

        // 구매자만 확정 가능하도록 체크
        if (!order.getBuyer().getUserId().equals(userId)) {
            throw new SecurityException("구매자 본인만 정산 완료를 확정할 수 있습니다.");
        }

        // 이미 DONE이면 리턴
        if (settlement.getPayoutStatus() == SettlementStatus.DONE) {
            return; // 이미 완료 → 그냥 리턴
        }

        // 1) Settlement 상태 변경
        settlement.setPayoutStatus(SettlementStatus.DONE);
        settlement.setPayoutAt(LocalDateTime.now());

        // 2) Order 상태 변경
        order.setOrderStatus("COMPLETED");
        order.setUpdatedAt(LocalDateTime.now());

        // 3) AuctionResult 상태 변경
        AuctionResultEntity result = order.getResult();
        if (result != null) {
            result.setResultStatus(ResultStatus.SUCCESS_COMPLETED);
        }
        settlementRepository.save(settlement);
    }

    @Transactional
    public void holdSettlement(SettlementEntity settlement, String reason) {
        settlement.setPayoutStatus(SettlementStatus.HOLD);
        settlement.setPayoutAt(null);
        settlementRepository.save(settlement);
    }
}
