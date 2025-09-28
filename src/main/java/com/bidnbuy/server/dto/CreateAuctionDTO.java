package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateAuctionDTO {
    private Long userId;
    private String title;
    private String description;
    private int start_price;
    private int min_bid_price;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
}
