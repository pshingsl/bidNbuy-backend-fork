package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuctionResultRepository extends JpaRepository<AuctionResultEntity, Long> {

    // 특정 경매 상품 아이디에 해당하는 최종결과(비로그인/상세페이지)
    Optional<AuctionResultEntity> findByAuction_AuctionId(Long auctionId);

    // 특정 사용자가 낙찰받은 상품의 모든 결과
    @Query("SELECT r FROM AuctionResultEntity r JOIN FETCH r.auction a WHERE r.winner.userId = :userId")
    List<AuctionResultEntity> findByWinner_UserId_Optimized(@Param("userId") Long userId);

    // 특정 사용자가 판매한 경매 상품의 모든 최종 결과를 조회
    @Query("SELECT r FROM AuctionResultEntity r JOIN FETCH r.auction a JOIN FETCH a.user u WHERE u.userId = :userId")
    List<AuctionResultEntity> findByAuction_User_UserId_Optimized(@Param("userId") Long userId);

}
