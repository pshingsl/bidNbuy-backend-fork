package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.paymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
//토스 승인 응답에서 필요한 최소 필드
public class PaymentResponseDto {
    private String paymentKey;
    private String orderId;     // = merchantOrderId (문자열)
    private Integer amount;
    private paymentStatus.PaymentMethod method;
    private paymentStatus.PaymentStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
}