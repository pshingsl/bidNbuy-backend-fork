package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * merchantOrderId로 주문 조회
     */
    public Optional<OrderEntity> findByMerchantOrderId(String merchantOrderId) {
        return orderRepository.findByMerchantOrderId(merchantOrderId);
    }

    /**
     * 신규 주문 저장
     */
    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }
}
