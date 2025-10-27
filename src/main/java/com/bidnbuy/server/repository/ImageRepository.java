package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// 리포지토리
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    @Query("SELECT i.imageUrl FROM ImageEntity i " +
            "WHERE i.auctionProduct.auctionId = :auctionId AND i.imageType = 'MAIN'")
    Optional<String> findMainImageUrl(Long auctionId);

    // 상품 상세 조회
    List<ImageEntity> findAllByAuctionProduct_AuctionId(Long auctionId);

    @Query("SELECT i.imageUrl FROM ImageEntity i " +
            "WHERE i.auctionProduct.auctionId = :auctionId " +
            "ORDER BY " +
            "    CASE WHEN i.imageType = 'MAIN' THEN 0 ELSE 1 END, " + // 1. MAIN 타입에 0점, PRODUCT 타입에 1점 부여 (0이 우선순위 높음)
            "    i.id ASC " + // 2. 그 다음 등록 순서대로 정렬
            "LIMIT 1")
    Optional<String> findFirstImageUrlByAuctionId(Long auctionId);

    Optional<ImageEntity> findByUser_UserId(Long userId);
}