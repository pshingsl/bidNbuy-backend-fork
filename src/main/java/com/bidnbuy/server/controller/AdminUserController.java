package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminUserListDto;
import com.bidnbuy.server.dto.AdminUserDetailDto;
import com.bidnbuy.server.dto.PagingResponseDto;
import com.bidnbuy.server.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    private final AdminUserService adminUserService;

    // 회원 목록 조회
    @GetMapping
    public ResponseEntity<PagingResponseDto<AdminUserListDto>> getUserList(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String email) {
        
        log.info("회원 목록 조회 요청: page={}, size={}, email={}", 
                pageable.getPageNumber(), pageable.getPageSize(), email);
        
        try {
            PagingResponseDto<AdminUserListDto> users = adminUserService.getUserList(pageable, email);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("회원 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 회원 상세 조회
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDto> getUserDetail(@PathVariable Long userId) {
        log.info("회원 상세 조회 요청: userId={}", userId);
        
        try {
            AdminUserDetailDto userDetail = adminUserService.getUserDetail(userId);
            return ResponseEntity.ok(userDetail);
        } catch (IllegalArgumentException e) {
            log.error("회원 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("회원 상세 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}