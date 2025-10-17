package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CreateInquiryRequest;
import com.bidnbuy.server.dto.InquiryResponse;
import com.bidnbuy.server.service.InquiriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiriesController {

    private final InquiriesService inquiriesService;

    // 1대1 문의 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> createInquiry(
            @RequestBody CreateInquiryRequest request,
            Principal principal
    ) {
        /**
         * 로그인 없이 테스트
          */
        Long userId = 1L;

       // Todo :JWT 인증 후 SecurityContext에서 userId를 가져온다고 가정
        // Long userId = Long.parseLong(principal.getName());

        InquiryResponse response = inquiriesService.createInquiry(userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "문의가 등록되었습니다");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }
}
