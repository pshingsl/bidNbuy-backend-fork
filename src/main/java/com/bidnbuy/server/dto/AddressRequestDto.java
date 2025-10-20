package com.bidnbuy.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.annotations.NotNull;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AddressRequestDto {
    @NotBlank(message="이름을 입력해주세요.")
    private String name;

    @NotBlank(message="전화번호을 입력해주세요.")
    private String phoneNumber;

    @NotBlank(message="우편번호을 입력해주세요.")
    private String zonecode;

    @NotBlank(message="도로명 또는 지번을 입력해주세요.")// 우편번호(신 우편 번호)
    private String address;

    @NotBlank(message="상세주소을 입력해주세요.")// 기본주소(도로명 또는 지번)
    private String detailAddress; // 상세 주소(아파트 동 호수)
}
