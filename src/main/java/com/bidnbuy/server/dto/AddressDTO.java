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
public class AddressDTO {
    private long addressId;
    private String zonecode;
    private String address;
    private char addressType;
    private String detailAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
