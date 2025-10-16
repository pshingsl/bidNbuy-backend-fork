package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.PaymentCancelEntity;
import com.bidnbuy.server.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentCancelRepository extends JpaRepository<PaymentCancelEntity, Long> {

}
