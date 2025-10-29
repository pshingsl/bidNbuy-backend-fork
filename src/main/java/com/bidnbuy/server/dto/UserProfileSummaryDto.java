package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileSummaryDto {
    private Long userId;
    private String nickname;
    private Double temperature;
    private String profileImageUrl;
    private long totalProductsCount; // 전체 판매 물품 개수
    private long salesCompletedCount; // 판매 완료된 거래 건수
}