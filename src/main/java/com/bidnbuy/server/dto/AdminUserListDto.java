package com.bidnbuy.server.dto;

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
@Schema(description = "관리자용 회원 목록")
public class AdminUserListDto {
    @Schema(description = "회원 ID", example = "1")
    private Long userId;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "닉네임", example = "사용자1")
    private String nickname;
    
    @Schema(description = "가입일", example = "2025-10-22T15:38:51.541941")
    private LocalDateTime createdAt;
    
    @Schema(description = "누적 페널티 점수", example = "10")
    private int penaltyPoints;
    
    @Schema(description = "활동 상태", example = "활동")
    private String activityStatus;
    
    @Schema(description = "정지 여부", example = "false")
    private boolean isSuspended;
    
    @Schema(description = "정지해제일", example = "2025-10-22T16:22:05.622491")
    private LocalDateTime suspendedUntil;
}