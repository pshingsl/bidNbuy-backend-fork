package com.bidnbuy.server.dto;

import lombok.Data;

@Data
public class BankAccountRequest {
    private String bankName;
    private String accountNumber;
    private String accountHolder;
}
