package com.bidnbuy.server.config;

import com.bidnbuy.server.security.CustomAuthenticationEntryPoint;
import com.bidnbuy.server.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
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
        http.cors(cors->cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptionHandling
                    ->exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
                
                //인증 경로 설정
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    .requestMatchers(HttpMethod.GET, "/auctions/**").permitAll() // 비로그인자도 다 볼 수있다.
                    .requestMatchers("/auth/signup", "/auth/login", "/auth/kakao","/favicon.ico", "/auth/naver", "/auth/reissue"
                            , "/auth/naver/loginstart", "/auth/email/**", "/auth/password/**", "/chat_test.html**", "/ws/bid/**", "/images/**").permitAll()
                    .requestMatchers("/chatrooms/**").authenticated()
                    .requestMatchers("/admin/auth/signup", "/admin/auth/login", "/admin/auth/reissue").permitAll() // 관리자 회원가입, 로그인, 토큰재발급 일단 허용
                    .requestMatchers("/orders/**", "/payments/**", "/inquiries/**").permitAll()  // ✅ 테스트용 오픈 - 강기병
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
}
