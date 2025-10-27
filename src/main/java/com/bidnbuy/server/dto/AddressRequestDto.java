package com.bidnbuy.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 10, message = "이름은 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "이름에는 특수 문자나 숫자를 포함할 수 없습니다.")
    private String name;

    @NotBlank(message="전화번호을 입력해주세요.")
    @Size(min = 10, max = 15, message = "유효한 전화번호 길이를 벗어났습니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "유효하지 않은 전화번호 형식입니다. (예: 010-1234-5678)")
    private String phoneNumber;

    @NotBlank(message="우편번호을 입력해주세요.")
    @Size(min = 5, max = 5, message = "우편번호는 5자리 숫자여야 합니다.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 숫자 5자리여야 합니다.")
    private String zonecode;

    @NotBlank(message="도로명 또는 지번을 입력해주세요.")
    @Size(max = 200, message = "주소 길이가 너무 깁니다.") // 우편번호(신 우편 번호)
    private String address;

    @NotBlank(message="상세주소을 입력해주세요.")// 기본주소(도로명 또는 지번)
    @Size(max = 100, message = "상세 주소 길이가 너무 깁니다.")
    private String detailAddress; // 상세 주소(아파트 동 호수)
}
