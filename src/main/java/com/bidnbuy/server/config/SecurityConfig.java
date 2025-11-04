package com.bidnbuy.server.config;

import com.bidnbuy.server.security.CustomAuthenticationEntryPoint;
import com.bidnbuy.server.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    @Autowired
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173", "https://d2f2dhfyp3k73e.cloudfront.net"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptionHandling
                        -> exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))

                //인증 경로 설정
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 비로그인 조회 허용 (임시 우회 추가 - /api 프리픽스)
                        .requestMatchers(HttpMethod.GET, "/auctions/**", "/api/auctions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/category/**", "/api/category/**").permitAll()
                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/kakao/**",  "/api/auth/kakao/**", "/favicon.ico", "/auth/naver", "/auth/reissue"
                                , "/auth/naver/loginstart", "/auth/email/**", "/auth/password/**", "/chat_test.html**", "/ws/bid/**", "/images/**").permitAll()
                        .requestMatchers("/chatrooms/**").authenticated()
                        .requestMatchers("/notifications/token/**").authenticated() //기타 알림 API는 인증 필요 여부에 따라 결정
                        .requestMatchers("/orders/**", "/payments/**", "/inquiries/**", "/reports/**").permitAll()  // ✅ 테스트용 오픈 - 강기병

                        .requestMatchers("/admin/auth/signup", "/admin/auth/login", "/admin/auth/reissue", "/admin/auth/password/**").permitAll() // 관리자 회원가입, 로그인, 토큰재발급, 비번재발급 일단 허용
                        .requestMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "ADMIN_VIEWER") // 관리자 읽기 전용 허용
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 나머지 관리자
                        .anyRequest().authenticated()
                ).csrf(csrf -> csrf.disable());

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );
        //요청마다 jwtAuthenticationFilter필터 실행하기
//        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
