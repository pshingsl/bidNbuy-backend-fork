package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.PaymentResponseDto;
import com.bidnbuy.server.dto.response.PaymentResponseDto;
import com.bidnbuy.server.entity.OrderEntity;
import com.bidnbuy.server.entity.PaymentEntity;
import com.bidnbuy.server.repository.OrderRepository;
import com.bidnbuy.server.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 결제 승인 성공 → Payment 저장
     */
    @Transactional
    public PaymentEntity saveConfirmedPayment(PaymentResponseDto dto) {
        // Order 찾기
        OrderEntity order = orderRepository.findByMerchantOrderId(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + dto.getOrderId()));

        // PaymentEntity 생성
        PaymentEntity payment = new PaymentEntity();
        payment.setOrder(order);
        payment.setMerchantOrderId(dto.getOrderId());
        payment.setTossPaymentKey(dto.getPaymentKey());
        payment.setTotalAmount(dto.getAmount());
        payment.setTossPaymentStatus(dto.getStatus());
        payment.setTossPaymentMethod(dto.getMethod());
        payment.setRequestedAt(dto.getRequestedAt());
        payment.setApprovedAt(dto.getApprovedAt());

        return paymentRepository.save(payment);
    }
}
