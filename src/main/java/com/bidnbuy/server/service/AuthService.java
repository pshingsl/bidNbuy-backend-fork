package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AuthResponseDto;
import com.bidnbuy.server.dto.TokenResponseDto;
import com.bidnbuy.server.entity.RefreshTokenEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthResponseDto login(String email, String password) {
        // 1. 사용자 인증 시도
        UserEntity loginUser = userService.findByEmailAndPassword(email, password);

        if (loginUser == null) {
            // 인증 실패 시 예외 처리
            throw new CustomAuthenticationException("Login failed. Check your email and password.");
        }

        // 2. 로그인 성공 시 access/refresh 토큰 생성
        String accessToken = jwtProvider.createAccessToken(loginUser.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(loginUser.getUserId());

        // 3. Refresh Token 만료시간 설정 및 DB 저장/갱신
//        long refreshTokenExpirationTime = jwtProvider.getAccessTokenExpirationTime();
//        Instant expiryDate = Instant.now().plusMillis(refreshTokenExpirationTime);

        Instant expiryDate = jwtProvider.getRefreshTokenExpiryDate();
        refreshTokenService.saveOrUpdate(loginUser, refreshToken, expiryDate);

        // 4. 응답 DTO 생성
        TokenResponseDto tokenInfo = TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType(jwtProvider.getGrantType())
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
                .build();

        return AuthResponseDto.builder()
                .email(loginUser.getEmail())
                .nickname(loginUser.getNickname())
                .tokenInfo(tokenInfo)
                .build();
    }

    @Transactional
    public AuthResponseDto reissue(String oldRefreshToken){
        //리프레시 토큰 유효성 검증
        if(!jwtProvider.validateToken(oldRefreshToken)){
            throw new CustomAuthenticationException("유효하지 않은 토큰");
        }

        //디비에서 토큰값으로 refresh entity 조회
        RefreshTokenEntity storedToken = refreshTokenService.findByTokenValue(oldRefreshToken)
                .orElseThrow(()->new CustomAuthenticationException("유효하지 않은 토큰"));

        UserEntity user = storedToken.getUser();

        //새 토큰들 생성
        Long userId = user.getUserId();
        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        Instant newExpiryDate = jwtProvider.getRefreshTokenExpiryDate();
        Long accessTokenExpiresIn = jwtProvider.getAccessTokenExpirationTime();

        //디비에 새 토큰으로 갱신
        refreshTokenService.saveOrUpdate(
                storedToken.getUser(),
                newRefreshToken,
                newExpiryDate
        );

        TokenResponseDto tokenInfo = TokenResponseDto.builder()
                .grantType(jwtProvider.getGrantType())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) // 새로 발급된 토큰 사용
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();

        return AuthResponseDto.builder()
                .tokenInfo(tokenInfo)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
