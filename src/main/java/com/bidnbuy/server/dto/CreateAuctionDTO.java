package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateAuctionDTO {
    private Long categoryId;
    //private List<ImageDTO> image;
    private String title;
    private String description;
    private Integer start_price;
    private Integer min_bid_price;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
}