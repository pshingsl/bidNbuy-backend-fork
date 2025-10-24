package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AdminInquiryListDto;
import com.bidnbuy.server.dto.AdminInquiryDetailDto;
import com.bidnbuy.server.dto.AdminInquiryReplyRequestDto;
import com.bidnbuy.server.dto.AdminInquiryStatusRequestDto;
import com.bidnbuy.server.enums.InquiryEnums;
import com.bidnbuy.server.service.AdminInquiriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiriesController {
    
    private final AdminInquiriesService adminInquiriesService;

    // 문의 목록 조회
    @GetMapping
    public ResponseEntity<Page<AdminInquiryListDto>> getInquiryList(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) InquiryEnums.InquiryType type,
            @RequestParam(required = false) InquiryEnums.InquiryStatus status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String userEmail) {
        
        log.info("관리자 문의 목록 조회 요청: page={}, size={}, type={}, status={}, title={}, userEmail={}", 
                pageable.getPageNumber(), pageable.getPageSize(), type, status, title, userEmail);
        
        try {
            Page<AdminInquiryListDto> inquiries = adminInquiriesService.getInquiryList(pageable, type, status, title, userEmail);
            return ResponseEntity.ok(inquiries);
        } catch (Exception e) {
            log.error("문의 목록 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 문의 상세 조회
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

    // 문의 답변 작성
    @PostMapping("/{inquiryId}/reply")
    public ResponseEntity<?> replyToInquiry(
            @PathVariable Long inquiryId,
            @RequestBody AdminInquiryReplyRequestDto request) {
        log.info("관리자 문의 답변 요청: inquiryId={}", inquiryId);
        
        try {
            adminInquiriesService.replyToInquiry(inquiryId, request);
            return ResponseEntity.ok().body("답변이 성공적으로 작성되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("문의 답변 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("문의 답변 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("답변 작성 중 오류가 발생했습니다.");
        }
    }

    // 문의 상태 변경
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