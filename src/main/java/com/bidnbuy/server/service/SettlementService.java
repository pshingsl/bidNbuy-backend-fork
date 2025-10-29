package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.SettlementEntity;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SettlementStatus;
import com.bidnbuy.server.repository.ChatRoomRepository;
import com.bidnbuy.server.repository.OrderRepository;
import com.bidnbuy.server.repository.SettlementRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;

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


        Long chatroomId = findChatRoomIdForOrder(order);

        String autoMessage = String.format(
                //자동 메세지 고정 내용
                "결제가 완료되었습니다. 주문번호 : %d 거래가 성공적으로 마무리되었습니다.", orderId
        );
        chatMessageService.sendAutoMessage(chatroomId, autoMessage);
        System.out.println("????????" + chatroomId);
    }

    @Transactional
    public void holdSettlement(SettlementEntity settlement, String reason) {
        settlement.setPayoutStatus(SettlementStatus.HOLD);
        settlement.setPayoutAt(null);
        settlementRepository.save(settlement);
    }

    // 정산완료 거래 완료 메세지 보내기
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
}
