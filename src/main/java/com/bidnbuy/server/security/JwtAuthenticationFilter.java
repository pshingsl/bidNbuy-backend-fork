package com.bidnbuy.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Autowired
    public JwtAuthenticationFilter(JwtProvider jwtProvider){
        this.jwtProvider = jwtProvider;
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        log.info("###################3shouldNotFilter path: {}", path);
//        if(path.startsWith("/auth")){
        if(path.startsWith("/auth/signup") || path.equals("/auth/login") || path.equals("/favicon.ico") || path.startsWith("/auth/kakao")
                || path.startsWith("/auth/naver")|| path.equals("/auth/reissue")|| path.startsWith("/auth/email")|| path.startsWith("/auth/password")
                || path.startsWith("/chat_test")|| path.startsWith("/ws/bid") 
                // ✅ 여기에 추가 - test용
//                || path.startsWith("/orders")
//                || path.startsWith("/payments")
        ){
            return true;
        }//인증 필터링 건너뛰기
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try{
            //요청 헤더에서 토큰 추출
            String token = parseBearerToken(request);
            log.info("filter is running for request:{}", request.getRequestURI());

            if(token !=null){
                //토큰 검증, 사용자id 추출
                String userIdStr = jwtProvider.validateAndGetUserId(token);
                //유효한 토큰
                if(userIdStr !=null){
                    Long userId = Long.valueOf(userIdStr);
                    log.info("Authenticated user Id : {}", userId);
                    
                    // 역할 기반 권한 설정
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    String role = jwtProvider.getRoleFromToken(token);
                    
                    if ("ADMIN".equals(role)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        log.info("Admin role assigned to user: {}", userId);
                    }
                    // 모든 사용자 기본으로 ROLE_USER 권한
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    
                    //Security Context에 저장할 인증 토큰 생성
                    AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,//비번 비밀
                            //임시 기본  권한 설정, 디비에 없더라고 시큐리티 인증을 위해 일반 사용자임을 알림
                            // AuthorityUtils.createAuthorityList("ROLE_USER")
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    authentication.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

//                    SecurityContext securityContext =  SecurityContextHolder.createEmptyContext();
//                    securityContext.setAuthentication(authentication);
//                    SecurityContextHolder.setContext(securityContext);
                }
            }
        }catch (Exception e){
            log.error("Security Context에 사용자 인증 정보를 설정할 수 없음", e);
        }
        filterChain.doFilter(request, response);
    }

    //HTTP 헤더에서 Bearer{Token} 형태 토큰 추출
    private String parseBearerToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        log.info("%%%%%%%%%%%%%%5Authorization Header: {}", bearerToken);
        if(StringUtils.hasText(bearerToken)&& bearerToken.startsWith("Bearer ")){
            //순수 토큰 값 반환
            return bearerToken.substring(7);
        }
        return null;
    }
}
