package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    Optional<SettlementEntity> findByOrder_OrderId(Long orderId);
}