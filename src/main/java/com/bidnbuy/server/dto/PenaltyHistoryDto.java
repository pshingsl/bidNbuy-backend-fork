package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.PenaltyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페널티 히스토리")
public class PenaltyHistoryDto {
    @Schema(description = "페널티 ID", example = "1")
    private Long penaltyId;
    
    @Schema(description = "페널티 타입", example = "LEVEL_1")
    private PenaltyType type;
    
    @Schema(description = "페널티 점수", example = "10")
    private int points;
    
    @Schema(description = "페널티 부과 일시", example = "2025-10-22T16:22:05.612027")
    private LocalDateTime createdAt;
    
    @Schema(description = "페널티 활성 여부", example = "true")
    private boolean isActive;
}