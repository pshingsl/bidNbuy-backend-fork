package com.bidnbuy.server.repository;

import com.bidnbuy.server.dto.AuctionHistoryDto;
import com.bidnbuy.server.entity.AuctionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistoryEntity, Long> {

    // 특정 경매 상품의 모든 상태 변경 이력을 시간 순(과거 -> 최신)으로 조회
    List<AuctionHistoryEntity> findAllByAuctionProduct_AuctionIdOrderByBidTimeAsc(Long auctionId);

    // 특정 경매 상품의 가장 최근 상태 변경 이력 1개를 조회
    Optional<AuctionHistoryEntity> findTopByAuctionProduct_AuctionIdOrderByBidTimeDesc(Long auctionId);
}
