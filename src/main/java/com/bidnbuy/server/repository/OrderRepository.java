package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.OrderEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 결제 대기 상태 + 24시간 지난 주문 조회
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.orderStatus = 'WAITING_PAYMENT' " +
            "AND o.createdAt < :deadline")
    List<OrderEntity> findExpiredOrders(@Param("deadline") LocalDateTime deadline);

    // 구매자가 본인인 경우
    @Query("SELECT o FROM OrderEntity o WHERE o.buyer.userId = :userId AND (:status IS NULL OR o.orderStatus = :status)")
    List<OrderEntity> findPurchaseOrders(@Param("userId") Long userId, @Param("status") String status);

    // 판매자가 본인인 경우
    @Query("SELECT o FROM OrderEntity o WHERE o.seller.userId = :userId AND (:status IS NULL OR o.orderStatus = :status)")
    List<OrderEntity> findSaleOrders(@Param("userId") Long userId, @Param("status") String status);

    // 온도 계산식
    @Query("SELECT AVG(o.rating) FROM OrderEntity o WHERE o.seller.userId = :sellerId AND o.rating > 0")
    Double getAverageRatingBySeller(@Param("sellerId") Long sellerId);

}
