package com.bidnbuy.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSignupRequestDto {
    @Schema(description = "관리자 이메일", example = "admin@bidnbuy.com", required = true)
    private String email;
    
    @Schema(description = "관리자 비밀번호 (영문, 숫자 포함 8자리 이상)", example = "admin123", required = true)
    private String password;
    
    @Schema(description = "관리자 닉네임", example = "관리자1", required = true)
    private String nickname;
    
    @Schema(description = "IP 수집 동의 여부", example = "true", required = true)
    private Boolean ipConsentAgreed;
}