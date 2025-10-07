package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionPurchaseHistoryDto {
    private Long auctionId;
    private String title;
    private String itemImageUrl;
    private String sellerNickname;
    private LocalDateTime endTime;
    private String status;
}
