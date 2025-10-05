package com.bidnbuy.server.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AuctionBidDto {
    private Long bidId;
    private Long userId;
    private Long auctionId;
    private Integer bidPrice;
    private LocalDateTime bidTime;
}
