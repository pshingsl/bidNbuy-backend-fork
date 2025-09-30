package com.bidnbuy.server.dto;

import lombok.Getter;

@Getter
public class KakaoUserInfoResponseDto {
    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        public static class Profile {
            private String nickname;
        }
    }
}
