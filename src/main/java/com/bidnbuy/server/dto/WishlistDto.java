package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistDto {
    private Long auctionId;
    private Integer wishCount ;
    private boolean isLiked;
}