package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionHistoryDto;
import com.bidnbuy.server.dto.WishlistDto;
import com.bidnbuy.server.service.AuctionHistoryService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "경매 이력 API", description = "경매 이력 기능 제공")
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class AuctionHistoryController {

    private final AuctionHistoryService historyService;

    @Operation(summary = "경매 이력 API", description = "경매 이력 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AuctionHistoryDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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
