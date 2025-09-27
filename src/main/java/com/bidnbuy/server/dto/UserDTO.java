package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.AuthStatus;
import com.bidnbuy.server.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDTO {
    private long userId;
    private long adminId;
    private long addressId;
    private String email;
    private String password;
    private String nickname;
    private AuthStatus authStatus;
    private UserStatus userStatus;
    private String userType;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private  LocalDateTime deletedAt;
}
