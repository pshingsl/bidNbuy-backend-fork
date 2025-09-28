package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//로그인 시 사용자 정보 서버 전달용 dto
@Getter
@Setter
public class LoginRequestDto {
    private String email;
    private String password;
}
