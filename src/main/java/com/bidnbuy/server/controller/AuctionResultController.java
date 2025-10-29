package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionPurchaseHistoryDto;
import com.bidnbuy.server.dto.AuctionSalesHistoryDto;
import com.bidnbuy.server.dto.MyPageSummaryDto;
import com.bidnbuy.server.dto.WishlistDto;
import com.bidnbuy.server.enums.TradeFilterStatus;
import com.bidnbuy.server.repository.AuctionResultRepository;
import com.bidnbuy.server.service.AuctionResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "마이페이지 경매 결과 API", description = "마이페이지 경매결과 기능 제공")
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class AuctionResultController {

    private final AuctionResultService auctionResultService;
    private final AuctionResultRepository auctionResultRepository;



    @Operation(summary = "마이페이지 조회 API", description = "마이페이지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MyPageSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<?> getMyPageSummaryDto(@AuthenticationPrincipal Long userId) {
        MyPageSummaryDto response = auctionResultService.getMyPageSummaryDto(userId);
        return ResponseEntity.ok().body(response);
    }

    // 마이페이지 - 구매 내역 (낙찰 내역) 조회
    @Operation(summary = "마이페이지 구매내역 조회 API", description = "마이페이지 구매내역 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AuctionPurchaseHistoryDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/purchase")
    public ResponseEntity<?> getPurchaseHistory(
            @AuthenticationPrincipal Long userId) {
        TradeFilterStatus defaultFilter = TradeFilterStatus.ALL;

        List<AuctionPurchaseHistoryDto> history = auctionResultService.getPurchaseHistory(userId, defaultFilter);

        // orderId 추가 - 강기병
        List<Map<String, Object>> result = history.stream()
                .map(dto -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("auctionId", dto.getAuctionId());
                    map.put("title", dto.getTitle());
                    map.put("status", dto.getStatus());
                    map.put("itemImageUrl", dto.getItemImageUrl());
                    map.put("sellerNickname", dto.getSellerNickname());
                    map.put("endTime", dto.getEndTime());

                    // orderId 별도 조회 (예: 서비스/리포지토리 통해)
                    Long orderId = auctionResultRepository.findOrderIdByAuctionId(dto.getAuctionId());
                    map.put("orderId", orderId);

                    return map;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "마이페이지 판매 내역 조회 API", description = "마이페이지 판매 내역 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AuctionSalesHistoryDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    // 마이페이지 - 판매 내역 (판매 결과)
    @GetMapping("/sales")
    public ResponseEntity<?> getSalesHistory(
            @AuthenticationPrincipal Long userId) {

        TradeFilterStatus defaultFilter = TradeFilterStatus.ALL;

        List<AuctionSalesHistoryDto> history = auctionResultService.getSalesHistory(userId, defaultFilter);

        return ResponseEntity.ok(history);
    }
}
