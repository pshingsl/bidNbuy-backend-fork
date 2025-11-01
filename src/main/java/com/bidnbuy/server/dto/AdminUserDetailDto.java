package com.bidnbuy.server.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자용 회원 상세")
public class AdminUserDetailDto {
    @Schema(description = "회원 ID", example = "1")
    private Long userId;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "닉네임", example = "사용자1")
    private String nickname;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
    
    @Schema(description = "가입일", example = "2025-10-22T15:38:51.541941")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일", example = "2025-10-22T16:22:05.622491")
    private LocalDateTime updatedAt;
    
    @Schema(description = "누적 페널티 점수", example = "10")
    private int penaltyPoints;
    
    @Schema(description = "활동 상태", example = "활동")
    private String activityStatus;
    
    @Schema(description = "정지 여부", example = "false")
    private boolean isSuspended;
    
    @Schema(description = "정지해제일", example = "2025-10-22T16:22:05.622491")
    private LocalDateTime suspendedUntil;
    
    @Schema(description = "정지 횟수", example = "0")
    private int suspensionCount;
    
    @Schema(description = "강퇴 횟수", example = "0")
    private int banCount;

    @Schema(description = "페널티 히스토리")
    @ArraySchema(arraySchema = @Schema(description = "페널티 내역 목록"))
    private List<PenaltyHistoryDto> penaltyHistory;

    @Schema(description = "거래글 개수", example = "5")
    private int auctionCount;
    
    @Schema(description = "사용자 타입", example = "GENERAL")
    private String userType;
    
    @Schema(description = "사용자 온도", example = "45.5")
    private Double userTemperature;
    
    @Schema(description = "탈퇴일", example = "2025-10-22T16:22:05.622491")
    private LocalDateTime deletedAt;
}