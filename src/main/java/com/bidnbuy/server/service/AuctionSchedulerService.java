package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.AuctionStatus;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuctionSchedulerService {
    private final AuctionProductsRepository auctionProductsRepository;
    private final AuctionBidRepository auctionBidsRepository;
    private final AuctionResultRepository auctionResultRepository;
    private final OrderRepository orderRepository;
    private final AuctionHistoryService auctionHistoryService;

    // 마감 시간이 된 경매를 처리하는 주요 스케줄러
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void closeFinishedAuctions() {
        List<AuctionProductsEntity> finishedAuctions = auctionProductsRepository.findByEndTimeBeforeAndSellingStatusNot(
                LocalDateTime.now(),
                SellingStatus.FINISH
        );

        if (finishedAuctions.isEmpty()) {
            return;
        }

        log.info("마감 시간이 된 경매 상품 수: {}", finishedAuctions.size());

        for (AuctionProductsEntity auction : finishedAuctions) {
            processAuctionClosing(auction);
        }
    }

    // 개별 경매 마감
    @Transactional
    private void processAuctionClosing(AuctionProductsEntity auction) {
        Optional<AuctionBidsEntity> topBidOpt =
                auctionBidsRepository.findTopByAuction_AuctionIdOrderByBidPriceDescBidTimeDesc(auction.getAuctionId());

        ResultStatus resultStatus;
        Integer finalPrice;
        AuctionBidsEntity finalBid = null;
        OrderEntity orderEntity = null;

        if (topBidOpt.isPresent()) {
            // ️ 낙찰 (SUCCESS)
            finalBid = topBidOpt.get();
            resultStatus = ResultStatus.SUCCESS_PENDING_PAYMENT;
            finalPrice = finalBid.getBidPrice();
            log.info("경매 낙찰 성공: 상품 ID {}, 낙찰가 {}원", auction.getAuctionId(), finalPrice);

            orderEntity = new OrderEntity();
            orderEntity.setSeller(auction.getUser());
            orderEntity.setBuyer(finalBid.getUser());
            orderEntity.setType("AUCTION");
            orderEntity.setOrderStatus("WAITING_PAYMENT");
            orderEntity.setRating(0);
            orderEntity.setCreatedAt(LocalDateTime.now());
            orderEntity.setUpdatedAt(LocalDateTime.now());

            //  orderEntity = orderRepository.save(orderEntity);
        } else {
            //  유찰 (FAILURE)
            resultStatus = ResultStatus.FAILURE;
            finalPrice = auction.getCurrentPrice();
            log.info("경매 유찰: 상품 ID {}", auction.getAuctionId());
        }

        // 1. AuctionResultEntity 생성 및 저장
        AuctionResultEntity result = AuctionResultEntity.builder()
                .auction(auction)
                .winner(finalBid != null ? finalBid.getUser() : null)
                .resultStatus(resultStatus)
                .finalPrice(finalPrice)
                // history_id는 DB 스키마에서 nullable=true여야 합니다.
                .history(finalBid != null ? finalBid.getHistory() : null)
                .order(orderEntity)
                .closedAt(LocalDateTime.now())
                .build();

       // AuctionResultEntity savedResult = auctionResultRepository.save(result);

        // 2. AuctionProductsEntity 상태 FINISH로 업데이트
        auction.setSellingStatus(SellingStatus.FINISH);

        //History 기록 (AuctionHistoryService의 독립 트랜잭션을 통해 안전하게 저장)
        auctionHistoryService.recordStatusChange(
                auction.getAuctionId(),
                AuctionStatus.FINISHED
        );

        // ❌ Builder를 이용한 중복 History 기록 로직과 헬퍼 메서드는 제거되었습니다.

        log.info("상품 ID {} 경매 마감 처리 완료.", auction.getAuctionId());
    }

    // 경매 도중에 판매자와 얘기해서 결재 성공했을때 경매 종료
    @Transactional
    public void closePaidAuctions(OrderEntity order) {
        //  결제 성공 상태인지 확인
        if (!order.getOrderStatus().equals("PAID")) {
            log.warn("❌ 결제 완료 상태가 아님: orderId={}", order.getOrderId());
            return;
        }

        // 2주문 타입이 ESCROW인지 확인
        if (!"ESCROW".equals(order.getType())) {
            log.info("⚠️ 경매 타입 주문이 아님. type={}, orderId={}", order.getType(), order.getOrderId());
            return;
        }

        // 해당 주문과 연결된 경매 결과 조회
        AuctionResultEntity result = auctionResultRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("해당 주문의 경매 결과가 존재하지 않습니다."));


        AuctionProductsEntity auction = result.getAuction();
//
//        // 이미 종료된 경매면 중복 종료방지
//        if (auction.getSellingStatus() == SellingStatus.FINISH) {
//            log.info("⚠️ 이미 종료된 경매입니다. auctionId={}", auction.getAuctionId());
//            return;
//        }
//
//        // 경매 상태 FINISH로 변경 및 저장
//        auction.setSellingStatus(SellingStatus.FINISH);
//        auctionProductsRepository.save(auction);
//
//        // 경매 결과 UCCESS_PAID 변경 및 저장
//        result.setResultStatus(ResultStatus.SUCCESS_PAID);
//        auctionResultRepository.save(result);
//
//        // 경매기록
//        auctionHistoryService.recordStatusChange(
//                auction.getAuctionId(),
//                AuctionStatus.FINISHED
//        );
//
//        log.info("💰 결제 완료로 인한 경매 강제 종료 처리 완료: 경매 ID {}", auction.getAuctionId());

        // ⭐ 무조건 새 Result 생성
        AuctionResultEntity result1 = AuctionResultEntity.builder()
                .auction(auction)
                .winner(order.getBuyer())
                .order(order)
                .finalPrice(auction.getCurrentPrice())
                .resultStatus(ResultStatus.SUCCESS_PAID)
                .closedAt(LocalDateTime.now())
                .build();

        auctionResultRepository.save(result1);

        auction.setSellingStatus(SellingStatus.FINISH);
        auctionProductsRepository.save(auction);

        auctionHistoryService.recordStatusChange(auction.getAuctionId(), AuctionStatus.FINISHED);

    }
}