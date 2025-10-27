package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BankAccountResponse {
    private String bankName;
    private String accountNumber;
    private String accountHolder;
}