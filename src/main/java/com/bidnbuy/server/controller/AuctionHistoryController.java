package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionHistoryDto;
import com.bidnbuy.server.service.AuctionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class AuctionHistoryController {

    private final AuctionHistoryService historyService;

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long auctionId) {
        List<AuctionHistoryDto> history = historyService.getHistory(auctionId, userId);

        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(history);
    }
}
