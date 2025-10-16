package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//외부 PG사 API 응답 매핑용
//백엔드 → Toss → 응답
@Getter
@Setter
public class TossCancelResponseDto {
    private String paymentKey;
    private String status;        // CANCELLED
    private Integer cancelAmount;
    private String cancelReason;
    private LocalDateTime cancelledAt;
}