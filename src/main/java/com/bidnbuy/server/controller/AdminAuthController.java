package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminLoginRequestDto;
import com.bidnbuy.server.dto.AdminSignupRequestDto;
import com.bidnbuy.server.dto.TokenResponseDto;
import com.bidnbuy.server.dto.UpdateIpRequestDto;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

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
}