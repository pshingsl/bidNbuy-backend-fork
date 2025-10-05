package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionBidDto;
import com.bidnbuy.server.dto.BidRequestDto;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.service.AuctionBidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bids/{auctionId}")
public class AuctionBidController {
    private final AuctionBidService auctionBidService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> placeBid(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal Long userId,
            @RequestBody BidRequestDto requestDto) {
        AuctionBidDto result = auctionBidService.bid(
                userId,
                auctionId,
                requestDto.getBidPrice()
        );

        ResponseDto<AuctionBidDto> response = ResponseDto.<AuctionBidDto>builder()
                .message(result.getBidPrice() + "원에 입찰되었습니다.")
                .item(result)
                .build();

        // 2. 응답 반환: 201 Created와 함께 최종 입찰 정보를 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

