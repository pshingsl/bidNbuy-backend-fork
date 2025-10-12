package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionPurchaseHistoryDto;
import com.bidnbuy.server.dto.AuctionSalesHistoryDto;
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

    // 마이페이지 - 구매 내역 (낙찰 내역) 조회
    @GetMapping("/purchase")
    public ResponseEntity<?> getPurchaseHistory(
            @AuthenticationPrincipal Long userId) { 

        List<AuctionPurchaseHistoryDto> history = auctionResultService.getPurchaseHistory(userId);

        if (history.isEmpty()) {
            return ResponseEntity.noContent().build(); // 내용이 없을 경우 204 No Content 반환
        }

        return ResponseEntity.ok(history);
    }


    // 마이페이지 - 판매 내역 (판매 결과)
    @GetMapping("/sales")
    public ResponseEntity<?> getSalesHistory(
            @AuthenticationPrincipal Long userId) {

        List<AuctionSalesHistoryDto> history = auctionResultService.getSalesHistory(userId);

        if (history.isEmpty()) {
            return ResponseEntity.noContent().build(); // 내용이 없을 경우 204 No Content 반환
        }

        return ResponseEntity.ok(history);
    }
}
