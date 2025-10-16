package com.bidnbuy.server.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCancelResponseDto {
    private String paymentKey;
    private String transactionKey;
    private String cancelReason;
    private Integer cancelAmount;
    private String cancelStatus;   // DONE
    private String receiptKey;
    private LocalDateTime canceledAt;
}