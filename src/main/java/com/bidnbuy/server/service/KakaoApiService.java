package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.KakaoTokenResponseDto;
import com.bidnbuy.server.dto.KakaoUserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
public class KakaoApiService {
    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoTokenResponseDto getKakaoAccessToken(String code){
        String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

        //헤더설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //바디설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        //http엔티티
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        //resttemplet으로 post요청
        ResponseEntity<KakaoTokenResponseDto> response = restTemplate.exchange(
                KAKAO_TOKEN_URL,
                HttpMethod.POST,
                request,
                KakaoTokenResponseDto.class
        );
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("카카오 Access Token 획득 실패");
    }

    public KakaoUserInfoResponseDto getKakaoUserInfo(String kakaoAccessToken) {
        String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

        //헤더 설정 (Authorization 헤더에 Bearer 토큰 추가)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);

        //HTTP 엔터티 생성 (헤더만)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // resttemplet으로 get요청
        ResponseEntity<KakaoUserInfoResponseDto> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                KakaoUserInfoResponseDto.class // 응답을 받을 DTO 타입 지정
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("카카오 사용자 정보 획득 실패");
    }
}
