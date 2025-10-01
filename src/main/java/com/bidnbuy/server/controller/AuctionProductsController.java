package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.service.AuctionProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService auctionProductsService; // 💡 Service 주입


    @PostMapping
    public ResponseEntity<?> createAuction(
            @AuthenticationPrincipal Long userId,
            // 💡 JSON 본문 전체를 DTO로 받습니다.
            @RequestBody @Valid CreateAuctionDto dto
    ) {
        List<ImageDto> images = dto.getImages();
        AuctionProductsEntity newProduct = auctionProductsService.create(dto,images,userId);

        // 2. 응답 DTO 생성 및 HTTP 201 Created 반환
        Map<String, Object> response = new HashMap<>();
        response.put("auctionId", newProduct.getAuctionId());
        response.put("title", newProduct.getTitle());
        response.put("message", "경매 상품과 이미지 정보가 성공적으로 등록되었습니다.");

        // DTO를 사용하지 않으므로, 이 방법이 가장 간결합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAuctionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ){
        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.getAuctionList(page, size);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionFind(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long auctionId
    ){
        AuctionFindDto find = auctionProductsService.getAuctionFind(auctionId, userId);
        return ResponseEntity.ok(find);
    }

}