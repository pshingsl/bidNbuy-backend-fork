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
            "ORDER BY i.imageType DESC, i.id ASC " +
            "LIMIT 1")
    Optional<String> findFirstImageUrlByAuctionId(Long auctionId);

    Optional<ImageEntity> findByUser_UserId(Long userId);
}