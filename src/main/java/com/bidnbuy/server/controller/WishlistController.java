package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionCreationResponseDto;
import com.bidnbuy.server.dto.WishlistDto;
import com.bidnbuy.server.dto.WishlistResponseDto;
import com.bidnbuy.server.enums.WishlistFilterStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bidnbuy.server.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "찜 API", description = "찜 기능 제공")
@RestController
@RequestMapping("/wishs")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "경매 상품 찜하기 소분류", description = "경매 상품 찜하기")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "찜하기 성공",
                    content = @Content(schema = @Schema(implementation = WishlistDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{auctionId}/like")
    public ResponseEntity<?> toggleLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long auctionId
    ) {
        // 로그인한 사용자가 아니면 401로 처리
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        // 결과
        WishlistDto response = wishlistService.like(userId, auctionId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "경매 상품 찜 조회 소분류", description = "경매 상품 찜 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "찜 조회 성공",
                    content = @Content(schema = @Schema(implementation = WishlistResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<?> likelist(
            @AuthenticationPrincipal Long userId
    ) {
        // 로그인한 사용자가 아니면 401로 처리
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        WishlistFilterStatus defaultFilter = WishlistFilterStatus.ALL;
        List<WishlistResponseDto> response = wishlistService.getWishlist(userId, defaultFilter);

        return ResponseEntity.ok(response);
    }

}