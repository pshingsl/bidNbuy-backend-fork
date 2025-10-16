package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId;
    private Long sellerId;
    private Long buyerId;
    private String type;
    private String orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}