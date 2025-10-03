package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.WishlistDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bidnbuy.server.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

}