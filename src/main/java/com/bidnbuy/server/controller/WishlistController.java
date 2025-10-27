package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.WishlistDto;
import com.bidnbuy.server.dto.WishlistResponseDto;
import com.bidnbuy.server.enums.WishlistFilterStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bidnbuy.server.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/wishs")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // POST /auctions/{auctionId}/like : 찜(좋아요) 상태를 토글(등록 또는 취소)
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