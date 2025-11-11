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
        // admin auth 쪽 스킵
        if (path.startsWith("/admin/auth/") || path.equals("/admin/auth")) {
            return true;
        }
        // auth 공개 엔드포인트 스킵
        if (
                path.equals("/auth/signup") ||
                path.equals("/auth/login") ||
                path.equals("/auth/reissue") ||
                path.startsWith("/auth/kakao") || path.startsWith("/api/auth/kakao") || path.startsWith("/auth/kakao/loginstart") || path.startsWith("/api/auth/kakao/loginstart") ||
                path.startsWith("/auth/naver") || path.startsWith("/api/auth/naver") || path.startsWith("/auth/naver/loginstart") || path.startsWith("/api/auth/naver/loginstart") ||
                path.startsWith("/auth/email") ||
                path.startsWith("/auth/password")
        ) {
            log.warn("### 필터 스킵 TRUE (Auth 공개): {}", path);
            return true;
        }
        // 공개 리소스 스킵
        if (path.equals("/favicon.ico") || path.startsWith("/chat_test") || path.startsWith("/ws/bid")) {
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

            if(StringUtils.hasText(token)){
                //토큰 검증, 사용자id 추출
                String userIdStr = jwtProvider.validateAndGetUserId(token);
                //유효한 토큰
                if(StringUtils.hasText(userIdStr)){
                    Long userId = Long.valueOf(userIdStr);
                    log.info("Authenticated user Id : {}", userId);
                    
                    // 역할 기반 권한 설정
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    String role = jwtProvider.getRoleFromToken(token);
                    
                    if ("ADMIN".equals(role)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        log.info("Admin role assigned to user: {}", userId);
                    } else if ("ADMIN_VIEWER".equals(role)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN_VIEWER"));
                        log.info("Admin viewer role assigned to user: {}", userId);
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
            // 퍼블릭 GET에서는 조용히 무시
            if (isPublicGet(request)) {
                log.debug("Invalid/failed token on public GET, continuing: {}", e.getMessage());
            } else {
                log.error("Security Context에 사용자 인증 정보를 설정할 수 없음", e);
            }
        }
        filterChain.doFilter(request, response);
    }

    //HTTP 헤더에서 Bearer{Token} 형태 토큰 추출
    private String parseBearerToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        log.info("%%%%%%%%%%%%%%5Authorization Header: {}", bearerToken);
        if (bearerToken == null) {
            return null;
        }
        bearerToken = bearerToken.trim();
        if (bearerToken.isEmpty() || "null".equalsIgnoreCase(bearerToken) || "undefined".equalsIgnoreCase(bearerToken)) {
            return null;
        }
        if (!bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = bearerToken.substring(7).trim();
        if (token.isEmpty() || "null".equalsIgnoreCase(token) || "undefined".equalsIgnoreCase(token)) {
            return null;
        }
        return token;
    }

    private boolean isPublicGet(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        return path.equals("/auctions") || path.startsWith("/auctions/")
                || path.equals("/api/auctions") || path.startsWith("/api/auctions/")
                || path.equals("/category") || path.startsWith("/category/")
                || path.equals("/api/category") || path.startsWith("/api/category/")
                || path.equals("/category/top") || path.equals("/api/category/top");
    }
}
