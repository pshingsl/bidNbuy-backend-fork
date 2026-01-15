package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.WishlistEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    // 사용자가 특정 경매 물품을 찜 했는지 확인
    // select * from wish_list where user = 1 and auction=1
    Optional<WishlistEntity> findByUserAndAuction(UserEntity user, AuctionProductsEntity auction);

    // select count(auction) from wishlist w where w.auction
    Integer countByAuction(AuctionProductsEntity auction);

    long countByAuction_AuctionId(Long auctionId);

    @Query("SELECT w FROM WishlistEntity w JOIN FETCH w.auction a WHERE a.deletedAt IS NULL AND w.user = :user")
    List<WishlistEntity> findByUserWithAuctionAndImages(UserEntity user);

    boolean existsByUser_UserIdAndAuction_AuctionId(Long userId, Long auctionId);

    // 특정 사용자가 현재 페이지 상품들 중 찜한 상품 ID 리스트만 일괄 조회
    @Query("SELECT w.auction.auctionId FROM WishlistEntity w " +
            "WHERE w.user.userId = :userId AND w.auction.auctionId IN :auctionIds")
    List<Long> findLikedAuctionIdsByUserIdAndAuctionIds(
            @Param("userId") Long userId,
            @Param("auctionIds") List<Long> auctionIds
    );
}
