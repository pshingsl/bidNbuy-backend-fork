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

            orderEntity = orderRepository.save(orderEntity);
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

        AuctionResultEntity savedResult = auctionResultRepository.save(result);

        if (orderEntity != null) {
            orderEntity.setResult(savedResult);
            orderRepository.save(orderEntity);
        }

        // 2. AuctionProductsEntity 상태 FINISH로 업데이트
        auction.setSellingStatus(SellingStatus.FINISH);

        // ✅ 3. History 기록 (AuctionHistoryService의 독립 트랜잭션을 통해 안전하게 저장)
        auctionHistoryService.recordStatusChange(
                auction.getAuctionId(),
                AuctionStatus.FINISHED
        );

        // ❌ Builder를 이용한 중복 History 기록 로직과 헬퍼 메서드는 제거되었습니다.

        log.info("상품 ID {} 경매 마감 처리 완료.", auction.getAuctionId());
    }
}