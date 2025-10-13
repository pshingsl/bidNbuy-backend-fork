package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByMerchantOrderId(String merchantOrderId);
}
