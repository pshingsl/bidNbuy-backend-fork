package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDto {
    private String email;
    private String nickname;

    private TokenResponseDto tokenInfo;

}
