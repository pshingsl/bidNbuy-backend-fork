package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionPurchaseHistoryDto;
import com.bidnbuy.server.dto.AuctionSalesHistoryDto;
import com.bidnbuy.server.dto.MyPageSummaryDto;
import com.bidnbuy.server.enums.TradeFilterStatus;
import com.bidnbuy.server.service.AuctionResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class AuctionResultController {

    private final AuctionResultService auctionResultService;

    @GetMapping
    public ResponseEntity<?> getMyPageSummaryDto(@AuthenticationPrincipal Long userId) {
        MyPageSummaryDto response = auctionResultService.getMyPageSummaryDto(userId);
        return ResponseEntity.ok().body(response);
    }

    // 마이페이지 - 구매 내역 (낙찰 내역) 조회
    @GetMapping("/purchase")
    public ResponseEntity<?> getPurchaseHistory(
            @AuthenticationPrincipal Long userId) {
        TradeFilterStatus defaultFilter = TradeFilterStatus.ALL;

        List<AuctionPurchaseHistoryDto> history = auctionResultService.getPurchaseHistory(userId, defaultFilter);

        return ResponseEntity.ok(history);
    }


    // 마이페이지 - 판매 내역 (판매 결과)
    @GetMapping("/sales")
    public ResponseEntity<?> getSalesHistory(
            @AuthenticationPrincipal Long userId) {

        TradeFilterStatus defaultFilter = TradeFilterStatus.ALL;

        List<AuctionSalesHistoryDto> history = auctionResultService.getSalesHistory(userId, defaultFilter);

        return ResponseEntity.ok(history);
    }
}
