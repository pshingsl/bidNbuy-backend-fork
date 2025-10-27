package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminLoginRequestDto;
import com.bidnbuy.server.dto.AdminSignupRequestDto;
import com.bidnbuy.server.dto.PasswordConfirmRequestDto;
import com.bidnbuy.server.dto.PasswordRequestDto;
import com.bidnbuy.server.dto.PasswordResetRequestDto;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.dto.TokenReissueRequestDto;
import com.bidnbuy.server.dto.TokenResponseDto;
import com.bidnbuy.server.dto.UpdateIpRequestDto;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.exception.CustomAuthenticationException;
import com.bidnbuy.server.service.AdminAuthService;
import com.bidnbuy.server.service.EmailService;
import com.bidnbuy.server.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AdminSignupRequestDto requestDto, HttpServletRequest request) {
        log.info("관리자 회원가입 요청: {}", requestDto.getEmail());
        
        try {
            AdminEntity savedAdmin = adminAuthService.signup(requestDto, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
        } catch (Exception e) {
            log.error("관리자 회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequestDto requestDto, HttpServletRequest request) {
        log.info("관리자 로그인 요청: {}", requestDto.getEmail());
        
        try {
            TokenResponseDto tokenResponse = adminAuthService.login(requestDto, request);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("관리자 로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/ip")
    public ResponseEntity<?> updateAllowedIp(@Valid @RequestBody UpdateIpRequestDto updateIpDto, HttpServletRequest request) {
        log.info("관리자 IP 업데이트 요청: {}", updateIpDto.getNewIpAddress());
        
        try {
            adminAuthService.updateAllowedIp(updateIpDto, request);
            return ResponseEntity.ok().body("IP 주소가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("관리자 IP 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 관리자 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@Valid @RequestBody TokenReissueRequestDto requestDto) {
        log.info("관리자 토큰 재발급 요청");
        
        try {
            TokenResponseDto reissueResponse = adminAuthService.reissueAdminToken(requestDto.getRefreshToken());
            return ResponseEntity.ok().body(reissueResponse);
        } catch (CustomAuthenticationException e) {
            log.error("관리자 토큰 재발급 실패: {}", e.getMessage());
            ResponseDto<String> responseDto = ResponseDto.<String>builder().error(e.getMessage()).build();
            return ResponseEntity.status(401).body(responseDto);
        } catch (Exception e) {
            log.error("관리자 토큰 재발급 실패: {}", e.getMessage());
            ResponseDto<String> responseDto = ResponseDto.<String>builder().error("Internal Server Error during token reissue.").build();
            return ResponseEntity.status(500).body(responseDto);
        }
    }

    // 관리자 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin(Authentication authentication) {
        log.info("관리자 로그아웃 요청");
        
        try {
            Object principal = authentication.getPrincipal();
            Long adminId;
            
            if (principal instanceof Long) {
                adminId = (Long) principal;
            } else {
                return ResponseEntity.status(401).build();
            }
            
            userService.logoutAdmin(adminId);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("관리자 로그아웃 중 에러 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 관리자 임시 비번
    @PostMapping("/password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        log.info("관리자 임시 비밀번호 요청: {}", request.getEmail());
        
        try {
            AdminEntity admin = adminAuthService.findByEmail(request.getEmail());
            String tempPassword = adminAuthService.generateAndSaveTempPassword(admin);
            emailService.sendTempPasswordEmailForAdmin(admin.getEmail(), tempPassword);
            
            return ResponseEntity.ok().body("임시 비밀번호가 이메일로 발송되었습니다.");
        } catch (Exception e) {
            log.error("관리자 임시 비밀번호 요청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 임시 비번 검증
    @PostMapping("/password/verify")
    public ResponseEntity<?> confirmPasswordUpdate(@RequestBody PasswordConfirmRequestDto requestDto) {
        log.info("관리자 임시 비밀번호 검증 요청: {}", requestDto.getEmail());
        
        try {
            adminAuthService.verifyTempPassword(requestDto.getEmail(), requestDto.getTempPassword());
            return ResponseEntity.ok().body("임시 비밀번호가 확인되었습니다. 새 비밀번호를 설정해 주세요.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().body("해당 관리자를 찾을 수 없습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("임시 비밀번호가 일치하지 않거나 만료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("알 수 없는 오류가 발생했습니다. 다시 시도해 주세요.");
        }
    }

    // 새 비밀번호 설정
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordRequestDto requestDto) {
        log.info("관리자 새 비밀번호 설정: {}", requestDto.getEmail());
        
        try {
            adminAuthService.finalResetPassword(requestDto.getEmail(), requestDto.getNewPassword());
            return ResponseEntity.ok().body("새 비밀번호가 성공적으로 설정되었습니다.");
        } catch (Exception e) {
            log.error("관리자 새 비밀번호 설정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}