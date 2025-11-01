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
@Schema(description = "관리자 IP 업데이트")
public class UpdateIpRequestDto {
    @Schema(description = "새 IP 주소", example = "203.0.113.5", required = true)
    private String newIpAddress;
}

// 일단 옵션.. ip 변경 필요 시