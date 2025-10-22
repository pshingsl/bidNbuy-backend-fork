package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.AuctionResultEntity;
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

    // 특정 사용자가 등록한 경매 물품 목록 조회
    List<AuctionProductsEntity> findByUser(UserEntity user);


    // 전체 상품을 가격 범위, 판매 상태, 삭제되지 않음 기준으로 필터링하여 조회합니다.
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE p.deletedAt IS NULL " + // 삭제되지 않은 상품만
            "AND p.sellingStatus IN :statuses " +
            "AND (:minPrice IS NULL OR p.currentPrice >= :minPrice) " + // 가격 하한 필터
            "AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)") // 가격 상한 필터
    Page<AuctionProductsEntity> findByPriceRangeAndStatusAndDeletedAtIsNull(
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("statuses") List<SellingStatus> statuses,
            Pageable pageable
    );

    @Query("SELECT p FROM AuctionProductsEntity p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE p.deletedAt IS NULL " + // 기본 조건: 삭제되지 않은 상품
            "AND p.sellingStatus IN :statuses " + // 경매 상태 필터 (필수)

            // 1. 검색 키워드 필터 (선택적)
            "AND (:#{#searchKeyword} IS NULL OR p.title LIKE %:#{#searchKeyword}%) " +

            // 2. 가격 범위 필터 (선택적)
            "AND (:#{#minPrice} IS NULL OR p.currentPrice >= :#{#minPrice}) " +
            "AND (:#{#maxPrice} IS NULL OR p.currentPrice <= :#{#maxPrice}) " +

            // 3. 카테고리 필터 (선택적)
            // mainCategoryId가 있다면 해당 카테고리 또는 그 자식 카테고리를 포함
            // subCategoryId가 있다면 정확히 그 카테고리를 필터
            "AND (" +
            "( :#{#mainCategoryId} IS NULL AND :#{#subCategoryId} IS NULL ) OR " + // 필터링 조건이 없을 때 통과

            // Sub Category 필터링 (Sub Category가 우선순위가 높다고 가정)
            "( :#{#subCategoryId} IS NOT NULL AND c.categoryId = :#{#subCategoryId} ) OR " +

            // Main Category 필터링 (Sub Category가 null일 때만 적용)
            "( :#{#mainCategoryId} IS NOT NULL AND :#{#subCategoryId} IS NULL AND (c.parent.categoryId = :#{#mainCategoryId} OR c.categoryId = :#{#mainCategoryId}) )" +
            ")")
    Page<AuctionProductsEntity> findDynamicFilteredAuctions(
            @Param("searchKeyword") String searchKeyword,
            @Param("mainCategoryId") Integer mainCategoryId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("statuses") List<SellingStatus> statuses,
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
            "LEFT JOIN FETCH p.images i " +
            "WHERE p.auctionId = :auctionId " +
            "AND p.deletedAt IS NULL")
    Optional<AuctionProductsEntity> findByIdWithDetails(Long auctionId);

    // 삭제
    Optional<AuctionProductsEntity> findByAuctionIdAndDeletedAtIsNull(Long auctionId);

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


    List<AuctionProductsEntity> findTop3ByUser_UserIdAndSellingStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long userId,
            List<SellingStatus> sellingStatuses // 💡 SellingStatusIn 조건에 의해 이 리스트가 매칭됨
    );


    List<AuctionProductsEntity> findByUser_UserIdAndSellingStatusInAndDeletedAtIsNull(
            Long userId,
            List<SellingStatus> sellingStatuses
    );

    // 이메일로 특정 유저 경매 상품 조회 - 관리자용
    @Query("SELECT p FROM AuctionProductsEntity p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH p.category c " +
            "WHERE p.deletedAt IS NULL " +
            "AND p.sellingStatus IN :statuses " +
            "AND u.email = :userEmail " +
            "AND (:#{#searchKeyword} IS NULL OR p.title LIKE %:#{#searchKeyword}%) " +
            "AND (:#{#minPrice} IS NULL OR p.currentPrice >= :#{#minPrice}) " +
            "AND (:#{#maxPrice} IS NULL OR p.currentPrice <= :#{#maxPrice}) " +
            "AND (" +
            "( :#{#mainCategoryId} IS NULL AND :#{#subCategoryId} IS NULL ) OR " +
            "( :#{#subCategoryId} IS NOT NULL AND c.categoryId = :#{#subCategoryId} ) OR " +
            "( :#{#mainCategoryId} IS NOT NULL AND :#{#subCategoryId} IS NULL AND (c.parent.categoryId = :#{#mainCategoryId} OR c.categoryId = :#{#mainCategoryId}) )" +
            ")")
    Page<AuctionProductsEntity> findByUserEmailAndStatuses(
            @Param("userEmail") String userEmail,
            @Param("statuses") List<SellingStatus> statuses,
            @Param("searchKeyword") String searchKeyword,
            @Param("mainCategoryId") Integer mainCategoryId,
            @Param("subCategoryId") Integer subCategoryId,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );
}