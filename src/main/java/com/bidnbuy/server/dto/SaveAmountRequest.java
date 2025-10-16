package com.bidnbuy.server.dto;

import lombok.Data;

@Data
public class SaveAmountRequest {
    private long orderId;
    private String merchantOrderId;
    private int amount;
}
