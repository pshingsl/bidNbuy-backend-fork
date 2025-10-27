package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuctionCreationResponseDto {
    private Long auctionId;
    private String title;
    private String message;
}
