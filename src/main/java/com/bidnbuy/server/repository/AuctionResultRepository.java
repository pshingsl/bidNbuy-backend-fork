package com.bidnbuy.server.repository;

import com.bidnbuy.server.dto.AuctionSalesHistoryDto;
import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.enums.ResultStatus;
import org.springframework.data.domain.Pageable;
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

    List<AuctionResultEntity> findByOrder_OrderId(Long orderId);

    // 마이페이지 최근 구매내역
    // List<AuctionResultEntity> findTop3ByWinner_UserIdOrderByAuction_EndTimeDesc(Long userId);

    // 유저프로필 판매완료 건수
    long countByAuction_User_UserIdAndResultStatus(Long userId, ResultStatus resultStatus);

    // winnerId로 조회하되 아직 order가 안 매핑된 가장 최근 결과 1개만
    Optional<AuctionResultEntity> findFirstByWinner_UserIdAndOrderIsNullOrderByClosedAtDesc(Long userId);

    // Order과 연결
    Optional<AuctionResultEntity> findByOrder(OrderEntity order);

    // auctionId로 orderId 조회 - 강기병
    @Query("SELECT r.order.orderId FROM AuctionResultEntity r WHERE r.auction.auctionId = :auctionId")
    Long findOrderIdByAuctionId(@Param("auctionId") Long auctionId);

    // 결재완료 시간 순서
    @Query("""
            SELECT r FROM AuctionResultEntity r
            JOIN r.order o
            WHERE r.winner.userId = :userId
            AND o.orderStatus = 'PAID'
            ORDER BY o.updatedAt DESC
            """)
    List<AuctionResultEntity> findTop3ByWinnerOrderByOrderUpdatedAtDesc(@Param("userId") Long userId);

    @Query("""
    SELECT new com.bidnbuy.server.dto.AuctionSalesHistoryDto(
        a.auctionId,
        a.title,
    (SELECT i.imageUrl FROM ImageEntity i WHERE i.auctionProduct.auctionId = a.auctionId AND i.imageType = 'MAIN'),
        a.startTime,
        a.endTime,
        r.finalPrice,
        u.nickname,
        r.resultStatus
    )
    FROM AuctionResultEntity r
    JOIN r.auction a
    JOIN r.winner u
    WHERE r.winner.userId = :userId
    ORDER BY a.endTime DESC
""")
    List<AuctionSalesHistoryDto> findPurchaseHistoryByUserId(@Param("userId") Long userId, Pageable pageable);

}
