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
public class CreateAuctionDto {
    private Long categoryId;
    //private List<ImageDTO> image;
    private String title;
    private String description;
    private Integer startPrice;
    private Integer minBidPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}