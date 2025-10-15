package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageSummaryDto {
    private List<AuctionPurchaseHistoryDto> recentPurchase;
    private List<AuctionSalesHistoryDto> recentSales;
}
