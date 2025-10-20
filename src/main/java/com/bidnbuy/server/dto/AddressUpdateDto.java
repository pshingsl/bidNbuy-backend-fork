package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AddressUpdateDto {
    private String name;

    private String phoneNumber;

    private String zonecode;

    private String address;

    private String detailAddress; // 상세 주소(아파트 동 호수)
}
