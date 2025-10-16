package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCancelRequestDto {
    private String paymentKey;      // Toss 결제 고유 키
    private String cancelReason;    // 취소 사유
    private Integer cancelAmount;   // 취소 금액 (null이면 전액 취소)
}