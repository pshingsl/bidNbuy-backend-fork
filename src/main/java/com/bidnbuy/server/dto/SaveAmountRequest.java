package com.bidnbuy.server.dto;

import lombok.Data;

@Data
public class SaveAmountRequest {
    private String merchantOrderId;
    private int amount;
}
