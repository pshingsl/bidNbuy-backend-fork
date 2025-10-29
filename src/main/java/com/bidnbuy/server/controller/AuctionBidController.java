package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.AuctionBidDto;
import com.bidnbuy.server.dto.AuctionCreationResponseDto;
import com.bidnbuy.server.dto.BidRequestDto;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.service.AuctionBidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "경매입찰 API", description = "상품 입찰 기능 제공")
@RestController
@RequiredArgsConstructor
@RequestMapping("/bids/{auctionId}")
public class AuctionBidController {
    private final AuctionBidService auctionBidService;

    @Operation(summary = "경매 입찰", description = "입찰시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "입찰 성공",
                    content = @Content(schema = @Schema(implementation = AuctionBidDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 데이터/유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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

    @Operation(summary = "경매 입찰 조회", description = "특정 경매 상품의 모든 입찰 기록을 조회 시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ResponseDto<List<AuctionBidDto>>> getBidsByAuction(
            @PathVariable Long auctionId) {

        // Service에서 입찰 기록 리스트
        List<AuctionBidDto> bidList = auctionBidService.getBidsByAuction(auctionId);

        String message;
        if (bidList.isEmpty()) {
            message = "아직 입찰 기록이 없습니다. 첫 입찰자가 되어보세요!";
        } else {
            Integer highestPrice = bidList.get(0).getBidPrice();

            message = String.format("현재 최고가는 %,d원입니다. (총 %d건의 입찰 기록 조회)",
                    highestPrice, bidList.size());
        }

        ResponseDto<List<AuctionBidDto>> response = ResponseDto.<List<AuctionBidDto>>builder()
                .message(message)
                .item(bidList)
                .build();

        // 조회는 200 OK를 반환합니다.
        return ResponseEntity.ok().body(response);
    }
}

