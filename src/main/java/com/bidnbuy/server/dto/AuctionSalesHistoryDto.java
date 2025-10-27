package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionSalesHistoryDto {
    private Long auctionId;
    private String title;
    private String itemImageUrl;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer finalPrice;

    // 낙찰자 정보
    private String winnerNickname;
    private String statusText;
}
