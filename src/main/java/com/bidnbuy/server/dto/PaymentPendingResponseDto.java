package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.PaymentEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentPendingResponseDto {
    private Long paymentId;
    private String merchantOrderId;
    private Integer totalAmount;
    private String status;

    public PaymentPendingResponseDto(PaymentEntity entity) {
        this.paymentId = entity.getPaymentId();
        this.merchantOrderId = entity.getMerchantOrderId();
        this.totalAmount = entity.getTotalAmount();
        this.status = entity.getTossPaymentStatus().name();
    }
}
