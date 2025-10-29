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

@Tag(name = "경매상품 API", description = "경매상품 기능 제공")
@RestController
@RequestMapping("/auctions")
public class AuctionProductsController {

    @Autowired
    private AuctionProductsService auctionProductsService;

    @Operation(summary = "상품 등록 API", description = "상품 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = AuctionCreationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 데이터/유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
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
    @Operation(summary = "상품 조회 API",
            description = "상품 조회",
            parameters = {
                    @Parameter(name = "page", description = "상품 페이지", required = false),
                    @Parameter(name = "size", description = "상품 개수", required = false),
                    @Parameter(name = "minPrice", description = "상품 최소가격", required = false),
                    @Parameter(name = "maxPrice", description = "상품 최대가격", required = false),
                    @Parameter(name = "sortBy", description = "상품 정렬", required = false),
                    @Parameter(name = "includeEnded", description = "상품 종료 유무", required = false),
                    @Parameter(name = "searchKeyword", description = "상품 검색", required = false),
                    @Parameter(name = "mainCategoryId", description = "상품 대분류", required = false),
                    @Parameter(name = "subCategoryId", description = "상품 소분류", required = false),
                    @Parameter(name = "userEmail", description = "유저 이메일(관리자 전용)", required = false),
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PagingResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "조회 실패")
    })
    @GetMapping
    public ResponseEntity<PagingResponseDto<AuctionListResponseDto>> getAllAuctions(
            @AuthenticationPrincipal Long userId,
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
                userId,
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

    @Operation(summary = "사용자 상품 상세 조회 API", description = "사용자 상품 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세조회 성공"),
            @ApiResponse(responseCode = "401", description = "상세조회 실패")
    })
    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionFind(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 경매 상품 ID", required = true)
            @PathVariable Long auctionId
    ) {
        AuctionFindDto find = auctionProductsService.getAuctionFind(auctionId, userId);
        return ResponseEntity.ok(find);
    }

    @Operation(summary = "사용자 상품 삭제 API", description = "사용자 상품 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "400", description = "경매 진행 상태(판매 중 등)로 인해 삭제 불가",
                    content = @Content(schema = @Schema(example = "경매가 이미 시작되었거나 낙찰되었습니다."))),
            @ApiResponse(responseCode = "403", description = "본인 상품이 아니어서 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<?> deleteAuction(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 경매 상품 ID", required = true)
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
    @Operation(summary = "관리자용 경매 상품 강제 삭제 API", description = "관리자 상품 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인이 필요함)"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (관리자 권한이 아님)"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 상품을 찾을 수 없음",
                    content = @Content(schema = @Schema(example = "해당 상품 존재하지 않습니다."))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(example = "삭제 중 오류가 발생했습니다.")))
    })
    @DeleteMapping("/admin/{auctionId}")
    public ResponseEntity<?> deleteAuctionByAdmin(@Parameter(description = "조회할 경매 상품 ID", required = true) @PathVariable Long auctionId) {
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