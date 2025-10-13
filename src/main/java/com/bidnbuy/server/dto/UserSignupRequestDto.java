package com.bidnbuy.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSignupRequestDto {
    @NotNull
    private String email;
    @NotNull
    private String password;
    @NotNull
    private String nickname;

    @NotNull
    private String validCode;
}
