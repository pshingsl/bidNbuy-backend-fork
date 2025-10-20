package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AddressResponseDto {
    private Long addressId;
    private String name; // 받는 사람
    private String phoneNumber; // 전화번호
    private String zonecode; // 우편번호(신 우편 번호)
    private String address; // 기본주소(도로명 또는 지번)
    private String detailAddress; // 상세 주소(아파트 동 호수)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
