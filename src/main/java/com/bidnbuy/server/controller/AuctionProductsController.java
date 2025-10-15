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
            @ModelAttribute @Valid CreateAuctionDto dto,
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

    // 전체 조회
    @GetMapping
    public ResponseEntity<PagingResponseDto<AuctionListResponseDto>> getAllAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
             @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "false") Boolean includeEnded
    ) {
        // 확장된 서비스 메서드 호출
        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.getAllAuctions(
                page,
                size,
                minPrice,
                maxPrice,
                sortBy,
                includeEnded
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

    // 검색
    @GetMapping("/search")
    public ResponseEntity<PagingResponseDto<AuctionListResponseDto>> searchAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "false") Boolean includeEnded
    ) {
        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.searchAuctions(
                page,
                size,
                searchKeyword,
                sortBy,
                includeEnded
        );
        return ResponseEntity.ok(list);
    }

    // 대분류
    @GetMapping("/filter/main")
    public ResponseEntity<PagingResponseDto<AuctionListResponseDto>> filterMainAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Integer mainCategoryId, // 대분류 ID (필수)
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "false") Boolean includeEnded
    ) {
        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.filterByMainCategory(
                page,
                size,
                mainCategoryId,
                minPrice,
                maxPrice,
                sortBy,
                includeEnded
        );
        return ResponseEntity.ok(list);
    }

    // 2. 소분류 필터링 엔드포인트: 정확히 해당 카테고리만 조회
    @GetMapping("/filter/sub")
    public ResponseEntity<PagingResponseDto<AuctionListResponseDto>> filterSubAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Integer subCategoryId, // 소분류 ID (필수)
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "false") Boolean includeEnded
    ) {
        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.filterBySubCategory(
                page,
                size,
                subCategoryId,
                minPrice,
                maxPrice,
                sortBy,
                includeEnded
        );
        return ResponseEntity.ok(list);
    }
}