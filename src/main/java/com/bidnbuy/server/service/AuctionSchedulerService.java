package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.AuctionBidsEntity;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.AuctionBidRepository;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.AuctionResultRepository;
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

        if (topBidOpt.isPresent()) {
            // ️ 낙찰 (SUCCESS)
            finalBid = topBidOpt.get();
            resultStatus = ResultStatus.SUCCESS_PENDING_PAYMENT; // 낙찰 후 결제 대기 상태로 시작
            finalPrice = finalBid.getBidPrice();
            log.info("경매 낙찰 성공: 상품 ID {}, 낙찰가 {}원", auction.getAuctionId(), finalPrice);

            // 💡 실제 구현: 여기서 OrderEntity를 생성하고 result.order에 연결해야 합니다.

        } else {
            //  유찰 (FAILURE)
            resultStatus = ResultStatus.FAILURE;
            finalPrice = auction.getCurrentPrice(); // 0 또는 시작가와 동일 (입찰이 없었으므로)
            log.info("경매 유찰: 상품 ID {}", auction.getAuctionId());
        }

        // 3. AuctionResultEntity 생성 및 저장
        AuctionResultEntity result = AuctionResultEntity.builder()
                .auction(auction)
                // 유찰 시 null, 낙찰 시 최고 입찰자 UserEntity
                .winner(finalBid != null ? finalBid.getUser() : null)
                .resultStatus(resultStatus)
                .finalPrice(finalPrice)
                // 최종 입찰 기록 (history FK)
                //.history(finalBid != null ? finalBid.getHistory() : null)
                // OrderEntity는 현재 생략 (null 처리)
                // .order(orderEntity)
                .closedAt(LocalDateTime.now())
                .build();

        auctionResultRepository.save(result);

        // 4. AuctionProductsEntity 상태 FINISH로 업데이트
        auction.setSellingStatus(SellingStatus.FINISH);
        // JPA의 Dirty Checking에 의해 트랜잭션 종료 시 자동 업데이트됨 (save 호출 불필요)

        log.info("상품 ID {} 경매 마감 처리 완료.", auction.getAuctionId());
    }
}

