package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.enums.SellingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionProductsRepository extends JpaRepository<AuctionProductsEntity, Long> {

    @Query("SELECT p FROM AuctionProductsEntity p " +
    "JOIN FETCH p.user u " +
    "JOIN FETCH p.category c " +
    "WHERE p.startTime <= CURRENT_TIMESTAMP AND p.endTime > CURRENT_TIMESTAMP " +
    "ORDER BY p.endTime ASC"
    )
    Page<AuctionProductsEntity> findRunningAuctionsWithDetails(Pageable pageable);

    @Query("SELECT p FROM AuctionProductsEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            "WHERE p.auctionId = :auctionId")
    Optional<AuctionProductsEntity> findByIdWithDetails(Long auctionId);

    // 삭제
    Optional<AuctionProductsEntity> findByAuctionIdAndDeletedAtIsNull(Long auctionId);

    // 핋터링
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            // 1. 상태 필터링 조건
            "WHERE p.sellingStatus IN :statuses " +
            // 2. 카테고리 필터링 조건
            "AND (:categoryId IS NULL OR c.categoryId = :categoryId) " +
            // 3. 검색어 조건 (제목 또는 설명)
            "AND (:searchKeyword IS NULL OR p.title LIKE %:searchKeyword% OR p.description LIKE %:searchKeyword%)" +
            //  4. 가격 범위 필터링 조건 추가
            "AND (:minPrice IS NULL OR p.currentPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)")
    Page<AuctionProductsEntity> findFilteredAuctionsByStatus(
            @Param("categoryId") Integer categoryId,
            @Param("searchKeyword") String searchKeyword,
            @Param("statuses") List<SellingStatus> statuses,
            // 가격 파라미터 추가
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );

    // 입찰 시 동시성 제어를 위한 락(LOCK)을 거는 조회 메서드 레디스를 안사용해서 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionProductsEntity a WHERE a.auctionId = :auctionId")
    Optional<AuctionProductsEntity> findByIdWithLock(Long auctionId);

    List<AuctionProductsEntity> findByEndTimeBeforeAndSellingStatusNot(
            LocalDateTime now,
            SellingStatus sellingStatus
    );

    //판매 중인 상품 조회
    Optional<AuctionProductsEntity> findByAuctionIdAndSellingStatus(Long auctionId, SellingStatus sellingStatus);

    Optional<AuctionProductsEntity> findByAuctionIdAndSellingStatusIn(
            Long auctionId,
            List<SellingStatus> sellingStatuses
    );
}