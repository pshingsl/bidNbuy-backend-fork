package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class OtherUserSalesDto {
    private Long auctionId;
    private String title;
    private String itemImageUrl;
}