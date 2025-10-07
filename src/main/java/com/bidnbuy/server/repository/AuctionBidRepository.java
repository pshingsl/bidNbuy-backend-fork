package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionBidsEntity;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.SellingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBidsEntity, Long> {
    // 사용자가 경매 특정 상품 입찰을 찾는 메서드
    Optional<AuctionBidsEntity> findByUserAndAuction(UserEntity user, AuctionProductsEntity auction);

    // 특정 겸매 물품의 현재 최고가를 찾는 메서드
    Optional<AuctionBidsEntity> findTopByAuctionOrderByBidPriceDesc(AuctionProductsEntity auction);

   List<AuctionBidsEntity> findByAuction_AuctionIdOrderByBidPriceDesc(Long  auctionId);

    Optional<AuctionBidsEntity> findTopByAuction_AuctionIdOrderByBidPriceDescBidTimeDesc(Long auctionId);
}
