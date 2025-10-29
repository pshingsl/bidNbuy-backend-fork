package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {
    private Long sellerId;
    private Long buyerId;
    private String type;
    private Long auctionId;// 거래 타입 (예: ESCROW)
}