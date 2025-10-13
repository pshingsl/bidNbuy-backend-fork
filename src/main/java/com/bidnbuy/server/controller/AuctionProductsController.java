package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.service.AuctionProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService auctionProductsService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAuction(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute  @Valid CreateAuctionDto dto,
            @RequestPart(value = "images") List<MultipartFile> imageFiles
    ) {

        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("경매 상품 이미지는 최소 1개 이상 필요합니다.");
        }

        AuctionProductsEntity newProduct = auctionProductsService.create(userId, dto, imageFiles);

        // 2. 응답 DTO 생성 및 HTTP 201 Created 반환
        AuctionCreationResponseDto response = AuctionCreationResponseDto.builder()
                .auctionId(newProduct.getAuctionId())
                .title(newProduct.getTitle())
                .message("경매 상품이 성공적으로 등록되었습니다.")
                .build();

        // DTO를 사용하지 않으므로, 이 방법이 가장 간결합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAuctionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "false") Boolean includeEnded,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {

        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.getAuctionList(
                page,
                size,
                categoryId,
                searchKeyword,
                includeEnded,
                sortBy,
                minPrice,
                maxPrice
        );
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionFind(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long auctionId
    ) {
        AuctionFindDto find = auctionProductsService.getAuctionFind(auctionId, userId);
        return ResponseEntity.ok(find);
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<?> deleteAuction(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long auctionId
    ) {
        try {
            auctionProductsService.deleteAuction(auctionId, userId);

            return ResponseEntity.noContent().build();

        } catch (IllegalStateException e) {
            // 포스트맨에서 삭제 메세지 확인하려고함
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}