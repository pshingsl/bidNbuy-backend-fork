package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserSignupResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;
}
