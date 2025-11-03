package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CreateInquiryRequest;
import com.bidnbuy.server.dto.InquiryResponse;
import com.bidnbuy.server.dto.RatingRequest;
import com.bidnbuy.server.service.InquiriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "문의 API", description = "문의 기능 제공")
@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiriesController {

    private final InquiriesService inquiriesService;

    // 문의 상세 조회
    @Operation(summary = "사용자 특정 문의 조회", description = "사용자 특정 문의 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = InquiryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{inquiryId}")
    public ResponseEntity<Map<String, Object>> getInquiryDetail(@PathVariable("inquiryId") Long inquiryId, @AuthenticationPrincipal Long userId) {
        InquiryResponse response = inquiriesService.getInquiryDetail(userId, inquiryId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    // 1대1 문의 조회
    @Operation(summary = "사용자 문의 조회", description = "사용자 문의 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(type = "object",
                            description = "문의 목록 (data: { inquiries: [InquiryResponse] })",
                            implementation = Map.class
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyInquiries(@AuthenticationPrincipal Long userId) {
        List<InquiryResponse> responseList = inquiriesService.getMyInquiries(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("data", Map.of("inquiries", responseList));

        return ResponseEntity.ok(result);
    }


    // 1대1 문의 등록
    @Operation(summary = "사용자 문의 등록", description = "사용자 등록 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(schema = @Schema(type = "object",
                            description = "등록된 문의 정보 (data: InquiryResponse)",
                            implementation = Map.class
                    ))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createInquiry(@RequestBody CreateInquiryRequest request, @AuthenticationPrincipal Long userId) {
        InquiryResponse response = inquiriesService.createInquiry(userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "문의가 등록되었습니다");
        result.put("data", response);

        return ResponseEntity.ok(result);
    }
}
