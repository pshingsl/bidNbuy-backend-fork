package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIpRequestDto {
    private String newIpAddress;
}

// 일단 옵션.. ip 변경 필요 시