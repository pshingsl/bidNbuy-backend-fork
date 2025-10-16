package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderUpdateRequestDto {
    private String status;  // 변경할 상태
    private String reason;  // 선택 입력
}