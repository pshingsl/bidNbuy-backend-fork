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
public class AdminLoginRequestDto {
    @Schema(description = "관리자 이메일", example = "admin@bidnbuy.com", required = true)
    private String email;
    
    @Schema(description = "관리자 비밀번호", example = "admin123", required = true)
    private String password;
}
