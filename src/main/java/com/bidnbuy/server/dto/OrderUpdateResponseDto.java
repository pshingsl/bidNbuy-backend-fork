package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderUpdateResponseDto {
    private Long orderId;
    private String message;
}
