package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.service.AuctionProductsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService auctionProductsService;

    @Operation(summary = "상품 등록", description = "상품 등록시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = CreateAuctionDto.class))),
            @ApiResponse(responseCode = "400", description = "등록 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAuction(
            @AuthenticationPrincipal Long userId,
            @Valid @ModelAttribute CreateAuctionDto dto,
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

    // 전체 조회 하나로 통일
    @Operation(summary = "상품 조회", description = "상품 조회시 사용되는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AuctionListResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "조회 실패")
    })
    @GetMapping
    public ResponseEntity<PagingResponseDto<AuctionListResponseDto>> getAllAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "false") Boolean includeEnded,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) Long mainCategoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) String userEmail
    ) {
        // 관리자용 이메일 조회
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                return ResponseEntity.status(403).build();
            }
        }

        PagingResponseDto<AuctionListResponseDto> list = auctionProductsService.getAllAuctions(
                page,
                size,
                minPrice,
                maxPrice,
                sortBy,
                includeEnded,
                searchKeyword,
                mainCategoryId,
                subCategoryId,
                userEmail
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
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류가 발생했습니다.");
        }
    }

    // 관리자용 경매 삭제
    @DeleteMapping("/admin/{auctionId}")
    public ResponseEntity<?> deleteAuctionByAdmin(@PathVariable Long auctionId) {
        try {
            auctionProductsService.deleteAuctionByAdmin(auctionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류가 발생했습니다.");
        }
    }

}