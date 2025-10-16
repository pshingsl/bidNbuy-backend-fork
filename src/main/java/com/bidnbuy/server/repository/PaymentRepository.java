package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByMerchantOrderId(String merchantOrderId);

    List<PaymentEntity> findByTossPaymentKeyAndTossPaymentKeyIsNotNull(String tossPaymentKey);

}
