package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AdminLoginRequestDto;
import com.bidnbuy.server.dto.AdminSignupRequestDto;
import com.bidnbuy.server.dto.TokenResponseDto;
import com.bidnbuy.server.dto.UpdateIpRequestDto;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.entity.RefreshTokenEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.repository.AdminRepository;
import com.bidnbuy.server.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    @Transactional
    public AdminEntity signup(AdminSignupRequestDto signupRequestDto, HttpServletRequest request) {
        String email = signupRequestDto.getEmail();
        String password = signupRequestDto.getPassword();
        String nickname = signupRequestDto.getNickname();
        Boolean ipConsentAgreed = signupRequestDto.getIpConsentAgreed();

        // 이메일 중복 체크
        if (adminRepository.existsByEmail(email)) {
            log.warn("admin email already exists: {}", email);
            throw new CustomAuthenticationException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 검증
        if (!password.matches(PASSWORD_REGEX)) {
            log.warn("password does not meet conditions: {}", password);
            throw new CustomAuthenticationException("비밀번호는 영문과 숫자를 포함해 8자리 이상이어야 합니다.");
        }

        // IP 수집 동의 체크
        if (ipConsentAgreed == null || !ipConsentAgreed) {
            throw new CustomAuthenticationException("IP 수집 동의가 필요합니다.");
        }

        // ip 추출
        String clientIp = getClientIpAddress(request);
        log.info("Admin signup request from IP: {}", clientIp);

        // AdminEntity 생성 및 저장
        AdminEntity newAdmin = AdminEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .ipAddress(clientIp)
                .build();

        return adminRepository.save(newAdmin);
    }

    public TokenResponseDto login(AdminLoginRequestDto loginRequestDto, HttpServletRequest request) {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();

        // 조회
        AdminEntity admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new CustomAuthenticationException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new CustomAuthenticationException("이메일 또는 비밀번호를 확인해주세요.");
        }

        // IP 검사 (화이트리스트 방식)
        String clientIp = getClientIpAddress(request);
        if (!isAllowedIp(clientIp, admin.getIpAddress())) {
            log.error("Admin login blocked - IP not allowed. Expected: {}, Actual: {}", admin.getIpAddress(), clientIp);
            throw new CustomAuthenticationException("허용되지 않은 IP에서의 접근입니다.");
        }

        // jwt 토큰 생성 -> role 포함
        String accessToken = jwtProvider.createAccessToken(admin.getAdminId(), "ADMIN");
        String refreshToken = jwtProvider.createRefreshToken(admin.getAdminId(), "ADMIN");

        // refresh token 저장 (관리자용 메서드로)
        Instant expiryDate = jwtProvider.getRefreshTokenExpiryDate();
        refreshTokenService.saveOrUpdateForAdmin(admin, refreshToken, expiryDate);
        
        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType(jwtProvider.getGrantType())
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
                .build();
    }

    // ip 업데이트 (미확정)
    // 현재는 로그인한 관리자가 본인 ip 변경하는 식
    @Transactional
    public void updateAllowedIp(UpdateIpRequestDto updateIpDto, HttpServletRequest request) {
        // id 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new CustomAuthenticationException("인증이 필요합니다.");
        }
        
        Long adminId = (Long) auth.getPrincipal();
        AdminEntity admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new CustomAuthenticationException("관리자 정보를 찾을 수 없습니다."));
        
        // 요청 ip랑 새 ip 일치하는지 확인
        String currentRequestIp = getClientIpAddress(request);
        if (!currentRequestIp.equals(updateIpDto.getNewIpAddress())) {
            throw new CustomAuthenticationException("요청 IP와 새 IP가 일치하지 않습니다.");
        }
        
        // ip 업데이트
        admin.setIpAddress(updateIpDto.getNewIpAddress());
        adminRepository.save(admin);
        
        log.info("Admin IP updated - AdminId: {}, New IP: {}", adminId, updateIpDto.getNewIpAddress());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // 여러 헤더에서 ip 추출 시도
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "X-Forwarded",
            "Forwarded-For",
            "Forwarded",
            "CF-Connecting-IP",
            "True-Client-IP"
        };
        
        for (String headerName : headerNames) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null && !headerValue.isEmpty() && !"unknown".equalsIgnoreCase(headerValue)) {
                // 여러 ip 있을 경우 첫 번째 ip 사용
                String ip = headerValue.split(",")[0].trim();
                if (isValidIp(ip)) {
                    log.info("IP extracted from header {}: {}", headerName, ip);
                    return ip;
                }
            }
        }
        
        // 헤더에서 못 찾은 경우 직접 연결 ip 사용
        String directIp = request.getRemoteAddr();
        log.info("Using direct connection IP: {}", directIp);
        return directIp;
    }

    // ip 유효성 검증
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4 패턴 체크
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        if (ip.matches(ipv4Pattern)) {
            return true;
        }
        
        // IPv6 패턴 체크 (간단 버전)
        if (ip.contains(":")) {
            return true;
        }
        
        // localhost 체크
        if ("localhost".equalsIgnoreCase(ip)) {
            return true;
        }
        
        return false;
    }

    /**
     * IP 화이트리스트 검증
     * @param clientIp 요청한 클라이언트 IP
     * @param allowedIp 허용된 IP (회원가입 시 저장된 IP)
     * @return 허용 여부
     */
    private boolean isAllowedIp(String clientIp, String allowedIp) {
        if (clientIp == null || allowedIp == null) {
            return false;
        }
        
        // 일치하는 경우
        if (clientIp.equals(allowedIp)) {
            return true;
        }
        
        // 로컬 개발 환경 허용 (127.0.0.1, localhost)
        if (isLocalDevelopmentIp(clientIp)) {
            return true;
        }
        
        // 향후 확장 - CIDR 표기법 지원, 여러 IP 허용 등

        return false;
    }

    // 로컬 개발 환경 ip 체크
    private boolean isLocalDevelopmentIp(String ip) {
        return "127.0.0.1".equals(ip) || 
               "localhost".equals(ip) || 
               "0:0:0:0:0:0:0:1".equals(ip) ||
               "::1".equals(ip) ||
               ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.16.") ||
               ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") ||
               ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") ||
               ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") ||
               ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") ||
               ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") ||
               ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") ||
               ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") ||
               ip.startsWith("172.31.");
    }

    // 토큰 재발급
    @Transactional
    public TokenResponseDto reissueAdminToken(String oldRefreshToken) {
        // 유효성
        if (!jwtProvider.validateToken(oldRefreshToken)) {
            throw new CustomAuthenticationException("유효하지 않은 토큰");
        }

        // 조회
        RefreshTokenEntity storedToken = refreshTokenService.findByTokenValue(oldRefreshToken)
                .orElseThrow(() -> new CustomAuthenticationException("유효하지 않은 토큰"));

        AdminEntity admin = storedToken.getAdmin();

        // 새 토큰 생성 (role 포함)
        Long adminId = admin.getAdminId();
        String newAccessToken = jwtProvider.createAccessToken(adminId, "ADMIN");
        String newRefreshToken = jwtProvider.createRefreshToken(adminId, "ADMIN");
        Instant newExpiryDate = jwtProvider.getRefreshTokenExpiryDate();

        // db에 새 토큰 갱신
        refreshTokenService.saveOrUpdateForAdmin(admin, newRefreshToken, newExpiryDate);

        return TokenResponseDto.builder()
                .grantType(jwtProvider.getGrantType())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
                .build();
    }
}