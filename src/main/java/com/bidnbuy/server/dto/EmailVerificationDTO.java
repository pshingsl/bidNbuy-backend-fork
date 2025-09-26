package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.IsVerified;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class EmailVerificationDTO {
    private  long emailId;
    private long userId;
    private String email;
    private String validCode;
    private Instant expirationTime;
    private IsVerified isVerified;
    private LocalDateTime createdAt;
}
