package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.UserEntity;
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

    // 💡 [추가] 1. 특정 사용자가 등록한 경매 물품 목록 조회
    List<AuctionProductsEntity> findByUser(UserEntity user);

    // 전체 목록 조회 시 사용 (논리적 삭제 제외)
    Page<AuctionProductsEntity> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 전체 상품을 가격 범위, 판매 상태, 삭제되지 않음 기준으로 필터링하여 조회합니다.
     */
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE p.deletedAt IS NULL " + // 삭제되지 않은 상품만
            "AND (:minPrice IS NULL OR p.currentPrice >= :minPrice) " + // 가격 하한 필터
            "AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)") // 가격 상한 필터
    Page<AuctionProductsEntity> findByPriceRangeAndStatusAndDeletedAtIsNull(
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );

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

    // 검색(제목)
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            "WHERE p.sellingStatus IN :statuses " +
            // 🚨 수정: description 검색 조건 제거, title에만 LIKE 조건 적용
            "AND (:searchKeyword IS NULL OR p.title LIKE %:searchKeyword%)"
    )

    Page<AuctionProductsEntity> findByKeywordOrFilter(
            @Param("searchKeyword") String searchKeyword,
            @Param("statuses") List<SellingStatus> statuses,
            Pageable pageable
    );

    // 대분류 필터링
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            "WHERE p.sellingStatus IN :statuses " +
            "AND (:mainCategoryId IS NULL OR c.parent.categoryId = :mainCategoryId OR c.categoryId = :mainCategoryId) " +
            "AND (:minPrice IS NULL OR p.currentPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)")
    Page<AuctionProductsEntity> findByMainCategoryWithChildren(
            @Param("mainCategoryId") Integer mainCategoryId,
            @Param("statuses") List<SellingStatus> statuses,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );

    // 소분류/중분류 (의류/자켓)
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            "WHERE p.sellingStatus IN :statuses " +
            "AND c.categoryId = :subCategoryId " + // 🚨 정확히 해당 카테고리 ID와 매칭
            "AND (:minPrice IS NULL OR p.currentPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)")
    Page<AuctionProductsEntity> findBySubCategoryOnly(
            @Param("subCategoryId") Integer subCategoryId,
            @Param("statuses") List<SellingStatus> statuses,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );


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