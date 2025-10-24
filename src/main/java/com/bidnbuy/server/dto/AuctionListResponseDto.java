package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionListResponseDto {
    private Long auctionId;
    private String title;
    private Integer currentPrice; // 현재 최고 입찰가
    private LocalDateTime createdAt;
    private LocalDateTime endTime;
    private String mainImageUrl;  // 목록에서는 대표 이미지 URL 1개만 필요
    private String sellingStatus; // 진행 중, 종료 등
    //private String categoryName;  // 필터링이나 표시를 위해 포함
    private String sellerNickname;
    private Integer wishCount;
}