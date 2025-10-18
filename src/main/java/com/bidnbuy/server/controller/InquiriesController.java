package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CreateInquiryRequest;
import com.bidnbuy.server.dto.InquiryResponse;
import com.bidnbuy.server.service.InquiriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiriesController {

    private final InquiriesService inquiriesService;

    // 문의 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<Map<String, Object>> getInquiryDetail(@PathVariable("inquiryId") Long inquiryId, @AuthenticationPrincipal Long userId) {
        InquiryResponse response = inquiriesService.getInquiryDetail(userId, inquiryId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    // 1대1 문의 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyInquiries(@AuthenticationPrincipal Long userId) {
        List<InquiryResponse> responseList = inquiriesService.getMyInquiries(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", Map.of("inquiries", responseList));

        return ResponseEntity.ok(result);
    }


    // 1대1 문의 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createInquiry(@RequestBody CreateInquiryRequest request, @AuthenticationPrincipal Long userId) {
        InquiryResponse response = inquiriesService.createInquiry(userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "문의가 등록되었습니다");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }
}
