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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 회원 관리 API", description = "관리자 회원 관리 기능 제공")
@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    
    private final AdminUserService adminUserService;

    @Operation(summary = "회원 목록 조회", description = "회원 목록 조회 (탈퇴/강퇴 회원 포함)", tags = {"관리자 회원 관리 API"})
    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = false, example = "0")
    @Parameter(name = "size", description = "페이지 크기", required = false, example = "20")
    @Parameter(name = "sort", description = "정렬 기준 (기본값 createdAt,DESC)", required = false, example = "createdAt,DESC")
    @Parameter(name = "email", description = "이메일로 검색", required = false, example = "user@example.com")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PagingResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
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

    @Operation(summary = "회원 상세 조회", description = "회원 상세 조회", tags = {"관리자 회원 관리 API"})
    @Parameter(name = "userId", description = "상세 조회 회원 ID", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = AdminUserDetailDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (회원을 찾을 수 없음)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
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