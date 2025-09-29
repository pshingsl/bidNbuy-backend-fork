package com.bidnbuy.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.jsonwebtoken.Jwts;

@Slf4j
@Service
public class JwtProvider {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration-time}")
    private long EXPIRATION_TIME;

    @Value("${jwt.refresh-expiration-time}")
    private long REFRESH_EXPIRATION_TIME;


    private static final String BEARER_TYPE = "Bearer";

    private final Map<SecretKey, JwtParser> parserCache = new ConcurrentHashMap<>();

    //시크릿 키 디코딩해서 key객체로 변환
    private SecretKey getSigningKey(){
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //검증위한 jwt파서 인스턴스를 캐싱해 반환
    private JwtParser getParser() {
        SecretKey key = getSigningKey();
        return parserCache.computeIfAbsent(key, k -> Jwts.parser()
                .verifyWith(k)
                .build());
    }

    // jwt토큰 생성
    public String createAccessToken(long userId){
        Date now = new Date();
        //만료시간 설정
        Date expiration = Date.from(Instant.now()
                .plus(EXPIRATION_TIME, ChronoUnit.MILLIS));

        return Jwts.builder()
                .signWith(getSigningKey(), Jwts.SIG.HS512) //알고리즘 키 설정
                .setSubject(String.valueOf(userId)) //토큰 사용자
                .setIssuer("bidnbuy-api")  //토큰 발급자
                .setIssuedAt(now) //발급시간
                .setExpiration(expiration)//만료시간
                .compact();//생성, 직렬화
    }

    //refresh token 생성
    public String createRefreshToken(long userId){
        Date now = new Date();
        Date expiration = Date.from(Instant.now()
                .plus(REFRESH_EXPIRATION_TIME, ChronoUnit.MILLIS));

        try{
            String token =Jwts.builder()
                .signWith(getSigningKey(), Jwts.SIG.HS512) //알고리즘 키 설정
                .setSubject(String.valueOf(userId)) //토큰 사용자
                .setIssuer("bidnbuy-api")  //토큰 발급자
                .setIssuedAt(now) //발급시간
                .setExpiration(expiration)//만료시간
                .compact();//생성, 직렬화
            log.info("refresh token created success for user :{}", userId);
            return token;
        }catch (Exception e){
            log.error("Refresh Token Creation Failed for user {}! Cause: {}", userId, e.getMessage(), e);
        }
        return null;
    }

    //jwt토큰 검증, 파싱
    public String validateAndGetUserId(String token){
        try{
            Claims claims = getParser()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();//토큰에서 userId추출
        }catch (SecurityException| MalformedJwtException e){
            log.error("Invalid JWT signature: {}", e.getMessage());
        }catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return null;
    }

    //토큰 유효성 검증
    public boolean validateToken(String token){
        return validateAndGetUserId(token) !=null;
    }

    //RTR(리프레시토큰 회전)을 위한 메서드
    public Long getUserIdFromToken(String token){
        try{
            String userId = getParser()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(userId);
        }catch (Exception e){
            log.error("error extracting UserId from token:{}", e.getMessage());
            throw new RuntimeException("토큰에서 사용자 정보를 추출할 수 없음");
        }
    }

    //refresh token 만료시간 instant객체로 반환
    public Instant getRefreshTokenExpiryDate(){
        return Instant.now().plus(REFRESH_EXPIRATION_TIME, ChronoUnit.MILLIS);
    }

    public long getAccessTokenExpirationTime(){
        return EXPIRATION_TIME;
    }

    public String getGrantType(){
        return BEARER_TYPE;
    }
}
