package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CreateAuctionDTO;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.service.AuctionProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService auctionProductsService;

    // 등록
    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateAuctionDTO dto
    ) {
        Long finalUserId = userId;
        if (userId == null) {
            finalUserId = 1L;
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body("로그인된 사용자만 상품을 등록");
        }
        try {
            Long newAuctionId = auctionProductsService.createAuctionProduct(dto, finalUserId);

            // 2. HTTP 201 Created 응답과 함께 생성된 상품 ID를 반환합니다.
            Map<String, Long> response = Map.of("auctionId", newAuctionId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            // 3. 예외가 발생한 경우, 400 Bad Request와 함께 에러 메시지를 반환합니다.
            //    (예: 카테고리가 없거나, 필수 필드가 누락된 경우 등)
            String error = "상품 등록 중 오류 발생: " + e.getMessage();

            // ResponseDto를 사용하거나 Map을 사용하여 에러 정보를 구성
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
    }
}