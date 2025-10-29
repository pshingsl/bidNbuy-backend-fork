package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
//프론트(또는 리다이렉트 후 승인 단계)에서 서버로 올라오는 값
public class PaymentRequestDTO {
    private String paymentKey;  // 토스에서 발급해준 결제 키
    private String orderId;     //  = merchantOrderId (우리가 만든 주문번호)
    private Integer amount;     // 결제 금액
    private Long auctionId;
}
