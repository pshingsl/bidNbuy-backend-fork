package com.bidnbuy.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
