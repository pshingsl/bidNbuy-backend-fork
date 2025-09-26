package com.bidnbuy.server.dto;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AdminDTO {
    private long adminId;
    private String email;
    private String password;
    private String name;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
}
