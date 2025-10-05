package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BidUpdateDto {
    private Long auctionId; // 어떤 경매가 갱신되었는지
    private Integer currentPrice; // 새로 갱신된 현재 최고가
    private Integer bidCount; // 새로 갱신된 입찰 횟수
    private Long lastBidderId; // 마지막 입찰자 ID (선택 사항)
}
