package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WishlistResponseDto {
    private Long auctionId;
    private String title;
    private String mainImageUrl;

    // 2. 가격 및 시간 정보
    private Integer currentPrice;
    private LocalDateTime endTime;

    // 3. 판매자 정보
    private String sellerNickname;

    // 4. 상태 정보
    private String sellingStatus;
}
