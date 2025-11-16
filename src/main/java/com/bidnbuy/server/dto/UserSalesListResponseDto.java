package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserSalesListResponseDto {
    private Integer completedSalesCount;
    private List<OtherUserSalesDto> completedSales;
    private Integer onSaleCount;
    private List<OtherUserSalesDto> onSale;
}