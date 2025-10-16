package com.bidnbuy.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
//토스 승인 응답에서 필요한 최소 필드 (서비스단에서 타입 변환예정)
public class PaymentResponseDto {
    private String paymentKey;
    private String orderId;     // = merchantOrderId (문자열)
    private Integer amount;
    private String method;
    private String status;
    private String requestedAt;
    private String approvedAt;
}