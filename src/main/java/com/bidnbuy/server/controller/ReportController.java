 package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CreateReportRequestDto;
import com.bidnbuy.server.dto.ReportListResponseDto;
import com.bidnbuy.server.dto.ReportResponseDto;
import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.service.ReportSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

 @RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportSevice reportService;

    // 신고 상세 조회
    // 신고 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> getReportDetail(@PathVariable Long reportId, @AuthenticationPrincipal Long userId) {
        ReportResponseDto report = reportService.getReportDetail(reportId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", report);

        return ResponseEntity.ok(response);
    }

    // 내 신고 목록 조회
    @GetMapping
    public ResponseEntity<ReportListResponseDto> getMyReports(@AuthenticationPrincipal Long userId) {
        List<ReportResponseDto> reports = reportService.getMyReports(userId);

        return ResponseEntity.ok(new ReportListResponseDto(reports));
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody CreateReportRequestDto request, @AuthenticationPrincipal Long userId) {
        InquiriesEntity report = reportService.createReport(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "신고가 접수되었습니다");
        response.put("data", Map.of(
                "reportId", report.getInquiriesId(),
                "title", report.getTitle(),
                "content", report.getContent(),
                "status", report.getStatus().name(),
                "createdAt", report.getCreatedAt()
        ));

        return ResponseEntity.ok(response);
    }
}
