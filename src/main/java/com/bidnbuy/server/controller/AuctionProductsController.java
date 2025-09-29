package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CreateAuctionDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.service.AuctionProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService auctionProductsService;

    // 등록
    @PostMapping
    public ResponseEntity<?> create(
            @AuthenticationPrincipal long userId,
            @RequestBody @Valid CreateAuctionDto dto
    ){
        try {
            AuctionProductsEntity newProduct = auctionProductsService.create(dto, userId);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "경매 상품이 성공적으로 등록되었습니다.",
                    "auctionId", newProduct.getAuctionId()
            );

            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
                Map<String, String> error= Map.of(
                        "success", "false",
                        "message", "경매 상품 등록 중 오류가 발생했습니다: " + e.getMessage()
                );
                return ResponseEntity.badRequest().body(error);
            }
        }
    }
