package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.NaverTokenResponseDto;
import com.bidnbuy.server.dto.NaverUserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NaverApiService {

    private final RestTemplate restTemplate;

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.uri.redirect}")
    private String redirectUri;

    @Value("${naver.uri.token-url}")
    private String tokenUri;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.uri.user-info-url}")
    private String userInfoUri;

    public NaverTokenResponseDto getNaverAccessToken(String code, String state){
        //헤더설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //바디설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);
        body.add("state", state);

        //http엔티티
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        //rest template으로 post요청
        ResponseEntity<NaverTokenResponseDto> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                request,
                NaverTokenResponseDto.class
        );
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            System.out.println("@@@@@@@DEBUG: 획득된 Access Token: " + response.getBody().getAccessToken());

            return response.getBody();
        }
        throw new RuntimeException("네이버 Access Token 획득 실패");
    }

    public NaverUserInfoResponseDto getNaverUserInfo(String NaverAccessToken) {
//        System.out.println("########DEBUG: 사용자 정보 요청에 사용되는 토큰: " + NaverAccessToken);

        //헤더 설정 (Authorization 헤더에 Bearer 토큰 추가)
        HttpHeaders headers = new HttpHeaders();
//        System.out.println("%%%%%%%%%%DEBUG: Authorization 헤더 값: Bearer " + NaverAccessToken);

        headers.set("Authorization", "Bearer " + NaverAccessToken);

        //HTTP 엔터티 생성 (헤더만)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // rest template으로 get요청
        ResponseEntity<NaverUserInfoResponseDto> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                entity,
                NaverUserInfoResponseDto.class // 응답을 받을 DTO 타입 지정
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("네이버 사용자 정보 획득 실패");
    }
}
