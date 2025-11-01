package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminInquiryListDto;
import com.bidnbuy.server.dto.AdminInquiryDetailDto;
import com.bidnbuy.server.dto.AdminInquiryReplyRequestDto;
import com.bidnbuy.server.dto.AdminInquiryStatusRequestDto;
import com.bidnbuy.server.dto.PagingResponseDto;
import com.bidnbuy.server.enums.InquiryEnums;
import com.bidnbuy.server.service.AdminInquiriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "관리자 문의 관리 API", description = "관리자 문의 관리 기능 제공")
@Slf4j
@RestController
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiriesController {
    
    private final AdminInquiriesService adminInquiriesService;

    @Operation(summary = "문의 목록 조회", description = "문의 목록 조회(필터링 가능)", tags = {"관리자 문의 관리 API"})
    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = false, example = "0")
    @Parameter(name = "size", description = "페이지 크기", required = false, example = "20")
    @Parameter(name = "sort", description = "정렬 기준 (기본값 createdAt,DESC)", required = false, example = "createdAt,DESC")
    @Parameter(name = "type", description = "문의 타입", required = false, example = "REPORT")
    @Parameter(name = "status", description = "답변 상태", required = false, example = "WAITING")
    @Parameter(name = "title", description = "제목 검색 키워드", required = false, example = "환불")
    @Parameter(name = "userEmail", description = "작성자 이메일 검색 키워드", required = false, example = "user@example.com")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "문의 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PagingResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<PagingResponseDto<AdminInquiryListDto>> getInquiryList(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) InquiryEnums.InquiryType type,
            @RequestParam(required = false) InquiryEnums.InquiryStatus status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String userEmail) {
        
        log.info("관리자 문의 목록 조회 요청: page={}, size={}, type={}, status={}, title={}, userEmail={}", 
                pageable.getPageNumber(), pageable.getPageSize(), type, status, title, userEmail);
        
        try {
            PagingResponseDto<AdminInquiryListDto> inquiries = adminInquiriesService.getInquiryList(pageable, type, status, title, userEmail);
            return ResponseEntity.ok(inquiries);
        } catch (Exception e) {
            log.error("문의 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "문의 상세 조회", description = "문의 상세 조회(관리자 답변 포함)", tags = {"관리자 문의 관리 API"})
    @Parameter(name = "inquiryId", description = "상세 조회 문의 ID", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "문의 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = AdminInquiryDetailDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (문의를 찾을 수 없음)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{inquiryId}")
    public ResponseEntity<AdminInquiryDetailDto> getInquiryDetail(@PathVariable Long inquiryId) {
        log.info("관리자 문의 상세 조회 요청: inquiryId={}", inquiryId);
        
        try {
            AdminInquiryDetailDto inquiryDetail = adminInquiriesService.getInquiryDetail(inquiryId);
            return ResponseEntity.ok(inquiryDetail);
        } catch (IllegalArgumentException e) {
            log.error("문의 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("문의 상세 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "문의 답변 작성", description = "문의 답변 작성", tags = {"관리자 문의 관리 API"})
    @Parameter(name = "inquiryId", description = "답변할 문의 ID", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "답변 작성 성공",
            content = @Content(schema = @Schema(type = "string", example = "답변이 성공적으로 작성되었습니다."))),
        @ApiResponse(responseCode = "400", description = "요청 오류 (문의를 찾을 수 없음)",
            content = @Content(schema = @Schema(type = "string", example = "문의를 찾을 수 없습니다: 1"))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(type = "string", example = "답변 작성 중 오류가 발생했습니다.")))
    })
    @PostMapping("/{inquiryId}/reply")
    public ResponseEntity<?> replyToInquiry(
            @PathVariable Long inquiryId,
            @RequestBody AdminInquiryReplyRequestDto request,
            Authentication authentication) {
        log.info("관리자 문의 답변 요청: inquiryId={}", inquiryId);
        
        try {
            // 로그인한 관리자id 추출
            Long adminId = Long.parseLong(authentication.getName());
            adminInquiriesService.replyToInquiry(inquiryId, request, adminId);
            return ResponseEntity.ok().body("답변이 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("문의 답변 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("문의 답변 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("답변 작성 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "문의 상태 변경", description = "문의 상태 변경", tags = {"관리자 문의 관리 API"})
    @Parameter(name = "inquiryId", description = "상태 변경할 문의 ID", required = true, example = "1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상태 변경 성공",
            content = @Content(schema = @Schema(type = "string", example = "상태가 성공적으로 변경되었습니다."))),
        @ApiResponse(responseCode = "400", description = "요청 오류 (문의를 찾을 수 없음)",
            content = @Content(schema = @Schema(type = "string", example = "문의를 찾을 수 없습니다: 1"))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (관리자만 접근 가능)"),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(type = "string", example = "상태 변경 중 오류가 발생했습니다.")))
    })
    @PatchMapping("/{inquiryId}/status")
    public ResponseEntity<?> updateInquiryStatus(
            @PathVariable Long inquiryId,
            @RequestBody AdminInquiryStatusRequestDto request) {
        log.info("관리자 문의 상태 변경 요청: inquiryId={}, status={}", inquiryId, request.getStatus());
        
        try {
            adminInquiriesService.updateInquiryStatus(inquiryId, request.getStatus());
            return ResponseEntity.ok().body("상태가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("문의 상태 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("문의 상태 변경 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("상태 변경 중 오류가 발생했습니다.");
        }
    }
}