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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    @Autowired private Environment environment;
    @Value("${admin.trusted-proxies:}")
    private String trustedProxiesProp;
    
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
            log.warn("password does not meet conditions");
            throw new CustomAuthenticationException("비밀번호는 영문과 숫자를 포함해 8자리 이상이어야 합니다.");
        }

        // IP 수집 동의 체크
        if (ipConsentAgreed == null || !ipConsentAgreed) {
            log.warn("IP consent not agreed");
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

        AdminEntity savedAdmin = adminRepository.save(newAdmin);
        log.info("Admin signup successful: adminId={}, email={}, IP={}", savedAdmin.getAdminId(), email, clientIp);
        
        return savedAdmin;
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
        
        log.info("Admin login successful: adminId={}, email={}, IP={}", admin.getAdminId(), email, clientIp);
        
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
        
        // ip 정규화/검증
        String currentRequestIp = getClientIpAddress(request);
        String normalizedCurrent = normalizeIpToken(currentRequestIp);
        String requestedNewIpRaw = updateIpDto.getNewIpAddress();
        String normalizedNew = normalizeIpToken(requestedNewIpRaw);

        if (!isValidIp(normalizedNew)) {
            throw new CustomAuthenticationException("유효하지 않은 IP 형식입니다.");
        }

        // 비교, 정규화
        boolean same;
        if (normalizedCurrent.equals(normalizedNew)) {
            same = true;
        } else {
            try {
                InetAddress a = InetAddress.getByName(normalizedCurrent);
                InetAddress b = InetAddress.getByName(normalizedNew);
                same = a.equals(b);
            } catch (Exception e) {
                same = false;
            }
        }
        if (!same) {
            throw new CustomAuthenticationException("요청 IP와 새 IP가 일치하지 않습니다.");
        }

        // ip 업데이트
        admin.setIpAddress(normalizedNew);
        adminRepository.save(admin);
        
        log.info("Admin IP updated - AdminId: {}, New IP: {}", adminId, normalizedNew);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // 우선순위 수정
        // 1. CloudFront-Viewer-Address(콜론 앞)
        String cfViewer = request.getHeader("CloudFront-Viewer-Address");
        if (cfViewer != null && !cfViewer.isBlank()) {
            int idx = cfViewer.indexOf(':');
            String ip = (idx > 0 ? cfViewer.substring(0, idx) : cfViewer).trim();
            if (isValidIp(ip)) {
                String normalized = normalizeIpToken(ip);
                log.info("IP extracted from CloudFront-Viewer-Address: {}", normalized);
                return normalized;
            }
        }

        // 2. X-Forwarded-For(첫 IP)
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty() && isValidIp(first)) {
                String normalized = normalizeIpToken(first);
                log.info("IP extracted from X-Forwarded-For: {}", normalized);
                return normalized;
            }
        }

        // 3. X-Real-IP
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && isValidIp(xRealIp)) {
            String normalized = normalizeIpToken(xRealIp);
            log.info("IP extracted from X-Real-IP: {}", normalized);
            return normalized;
        }

        // 4. 직접 연결 IP (프록시 IP일 수 있음)
        String directIp = request.getRemoteAddr();
        log.info("Using direct connection IP (fallback): {}", directIp);
        return directIp;
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
        // 정규화 + InetAddress 비교
        String normClient = normalizeIpToken(clientIp);
        String normAllowed = normalizeIpToken(allowedIp);
        if (normClient.equals(normAllowed)) return true;
        try {
            InetAddress a = InetAddress.getByName(normClient);
            InetAddress b = InetAddress.getByName(normAllowed);
            if (a.equals(b)) return true;
        } catch (Exception ignored) {}

        // 개발 프로파일에서만 로컬/사설망 허용
        if (isDev() && isLocalDevelopmentIp(normClient)) {
            return true;
        }
        
        // 향후 확장?

        return false;
    }

    private boolean isDev() {
        try {
            String[] profiles = environment.getActiveProfiles();
            if (profiles == null) return false;
            for (String p : profiles) {
                if ("dev".equalsIgnoreCase(p)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean fromTrustedProxy(HttpServletRequest request) {
        if (trustedProxiesProp == null || trustedProxiesProp.isEmpty()) return false;
        String remote = request.getRemoteAddr();
        for (String cidr : getTrustedCidrs()) {
            if (new IpAddressMatcher(cidr).matches(remote)) return true;
        }
        return false;
    }

    private java.util.List<String> getTrustedCidrs() {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (trustedProxiesProp == null || trustedProxiesProp.isEmpty()) return list;
        for (String token : trustedProxiesProp.split(",")) {
            String t = token.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    private String extractClientIpFromXff(String xff, java.util.List<String> trustedCidrs) {
        if (xff == null || xff.isEmpty()) return null;
        String[] parts = xff.split(",");
        for (int i = parts.length - 1; i >= 0; i--) {
            String ip = normalizeIpToken(parts[i]);
            if (ip.isEmpty()) continue;
            // 신뢰 프록시 대역이면 스킵
            if (isTrusted(ip, trustedCidrs)) continue;
            return ip;
        }
        return null;
    }

    private boolean isTrusted(String ip, java.util.List<String> trustedCidrs) {
        if (ip == null) return false;
        String norm = normalizeIpToken(ip);
        for (String cidr : trustedCidrs) {
            if (new IpAddressMatcher(cidr).matches(norm)) return true;
        }
        return false;
    }

    // IPv4/IPv6 검증
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank()) return false;
        if ("localhost".equalsIgnoreCase(ip)) return true;
        String norm = normalizeIpToken(ip);
        String ipv4 = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        if (norm.matches(ipv4)) return true;
        if (norm.indexOf(':') >= 0 && norm.matches("^[0-9A-Fa-f:]+$")) {
            try {
                InetAddress addr = InetAddress.getByName(norm);
                return (addr instanceof Inet6Address);
            } catch (Exception ignored) {}
        }
        return false;
    }

    // 공통 정규화
    private String normalizeIpToken(String token) {
        if (token == null) return "";
        String s = token.trim();
        if (s.length() >= 2 && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        if (s.startsWith("[") && s.contains("]")) {
            int end = s.indexOf(']');
            if (end > 0) s = s.substring(1, end);
        }
        int colonCount = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == ':') colonCount++;
        if (colonCount == 1 && s.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\:[0-9]+$")) {
            s = s.substring(0, s.lastIndexOf(':'));
        }
        return s.trim();
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

        log.info("Admin token reissued successfully: adminId={}", adminId);

        return TokenResponseDto.builder()
                .grantType(jwtProvider.getGrantType())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
                .build();
    }

    // 임시 비번
    // 생성&저장
    public String generateAndSaveTempPassword(AdminEntity admin) {
        String tempPassword = generateRandomPassword();
        String hashedPassword = passwordEncoder.encode(tempPassword);
        
        admin.setTempPasswordHash(hashedPassword);
        admin.setTempPasswordExpiryDate(LocalDateTime.now().plusMinutes(10));
        
        adminRepository.save(admin);
        
        log.info("Admin temp password generated: adminId={}, email={}, expiry={}", 
                admin.getAdminId(), admin.getEmail(), admin.getTempPasswordExpiryDate());
        
        return tempPassword;
    }

    // 랜덤 생성
    private String generateRandomPassword() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        
        for (int i = 0; i < 8; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    // 검증
    public void verifyTempPassword(String email, String tempPw) {
        AdminEntity admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new CustomAuthenticationException("관리자를 찾을 수 없습니다."));
        
        if (!isTempPasswordValid(admin, tempPw)) {
            log.warn("Admin temp password verification failed: email={}", email);
            throw new RuntimeException("임시 비밀번호가 일치하지 않거나 만료되었습니다.");
        }
        
        log.info("Admin temp password verified: adminId={}, email={}", admin.getAdminId(), email);
    }

    // 유효성
    private boolean isTempPasswordValid(AdminEntity admin, String tempPw) {
        if (admin.getTempPasswordExpiryDate() == null || 
            admin.getTempPasswordExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        if (passwordEncoder.matches(tempPw, admin.getTempPasswordHash())) {
            return true;
        }
        return false;
    }

    // 초기화
    private void clearTempPassword(AdminEntity admin) {
        admin.setTempPasswordHash(null);
        admin.setTempPasswordExpiryDate(null);
        adminRepository.save(admin);
    }

    // 비번 업데이트
    public void updatePassword(AdminEntity admin, String newPassword) {
        String hashedPw = passwordEncoder.encode(newPassword);
        admin.setPassword(hashedPw);
        adminRepository.save(admin);
    }

    // 비번 재설정
    @Transactional
    public void finalResetPassword(String email, String newPassword) {
        AdminEntity admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new CustomAuthenticationException("관리자를 찾을 수 없습니다."));
        
        if (!newPassword.matches(PASSWORD_REGEX)) {
            throw new CustomAuthenticationException("비밀번호는 영문과 숫자를 포함해 8자리 이상이어야 합니다.");
        }
        
        clearTempPassword(admin);
        updatePassword(admin, newPassword);
        
        log.info("Admin password reset completed: adminId={}, email={}", admin.getAdminId(), email);
    }

    // 조회
    public AdminEntity findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new CustomAuthenticationException("관리자를 찾을 수 없습니다."));
    }
}