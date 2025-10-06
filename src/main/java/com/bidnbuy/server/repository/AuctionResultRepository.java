package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionResultRepository extends JpaRepository<AuctionResultEntity, Long> {

    // 특정 경매 상품 아이디에 해당하는 최종결과(비로그인/상세페이지)
    Optional<AuctionResultEntity> findByAuction_AuctionId(Long auctionId);

    // 특정 사용자가 낙찰받은 상품의 모든 결과
    List<AuctionResultEntity> findByWinner_UserId(Long userId);

    // 특정 사용자가 판매한(등록)경매 상품의 모든 최종 결과를 조회
    List<AuctionResultEntity> findByAuction_User_UserId(Long userId);
}
