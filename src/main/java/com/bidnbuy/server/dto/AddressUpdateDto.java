package com.bidnbuy.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AddressUpdateDto {

    @Schema(example = "홍길동", description = "수정할 이름 (선택 사항)", required = false)
    @Size(max = 10, message = "이름은 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "이름에는 특수 문자나 숫자를 포함할 수 없습니다.")
    private String name;

    @Schema(example = "010-1234-5678", description = "수정할 전화번호 (선택 사항)", required = false)
    @Size(min = 10, max = 15, message = "유효한 전화번호 길이를 벗어났습니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "유효하지 않은 전화번호 형식입니다. (예: 010-1234-5678)")
    private String phoneNumber;

    @Schema(example = "08500", description = "수정할 우편번호 (선택 사항)", required = false)
    @Size(min = 5, max = 5, message = "우편번호는 5자리 숫자여야 합니다.")
    private String zonecode;

    @Schema(example = "서울시 강남구 테헤란로", description = "수정할 기본 주소 (선택 사항)", required = false)
    @Size(max = 200, message = "주소 길이가 너무 깁니다.")
    private String address;

    @Schema(example = "101동 1204호", description = "수정할 상세 주소 (선택 사항)", required = false)
    @Size(max = 100, message = "상세 주소 길이가 너무 깁니다.")
    private String detailAddress; // 상세 주소(아파트 동 호수)
}
