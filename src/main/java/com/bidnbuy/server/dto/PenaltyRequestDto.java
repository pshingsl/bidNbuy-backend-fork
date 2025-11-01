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
@Schema(description = "페널티 부과")
public class PenaltyRequestDto {
    @Schema(description = "페널티 부과 유저 ID", example = "1", required = true)
    private Long userId;
    
    @Schema(description = "페널티 타입 (LEVEL_1: 10점, LEVEL_2: 30점, LEVEL_3: 50점)", example = "LEVEL_1", required = true, allowableValues = {"LEVEL_1", "LEVEL_2", "LEVEL_3"})
    private String type;
}