package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 생성자 자동화 주입 역할
public class AuctionProductsService {

    // 불변성을 보장하기 위해 final로 사용
    private final AuctionProductsRepository auctionProductsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final WishlistRepository wishlistRepository;
    private final AuctionHistoryService auctionHistoryService;

    // create -> 인증된 사용자만 등록 유저 검증 필요,
    @Transactional
    public AuctionProductsEntity create(CreateAuctionDto dto, List<ImageDto> images, Long userId) {

        // 유저 아이디 유효성 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 ID가 없습니다"));

        // 카테고리 조회 및 유효성 검증
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("해당 카테고리 ID가 없습니다"));

        // AuctionProductsEntity 생성
        AuctionProductsEntity auctionProducts = AuctionProductsEntity.builder()
                .title(dto.getTitle())
                .user(user)         // 연관 관계 설정
                .category(category) // 연관 관계 설정
                .description(dto.getDescription())
                .startPrice(dto.getStartPrice())
                .currentPrice(dto.getStartPrice())
                .minBidPrice(dto.getMinBidPrice())
                .sellingStatus(SellingStatus.PROGRESS)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .bidCount(0)
                .build();
        // 저장
        AuctionProductsEntity savedProduct = auctionProductsRepository.save(auctionProducts);
        auctionHistoryService.recordStatusChange(
                savedProduct.getAuctionId(),
                com.bidnbuy.server.enums.AuctionStatus.PROGRESS // AuctionStatus Enum 사용
        );

        // 이미지 저장
        if (images != null) {
            for (ImageDto imageDto : images) {
                ImageEntity image = ImageEntity.builder()
                        .auctionProduct(auctionProducts)
                        .imageUrl(imageDto.getImageUrl())
                        .imageType(imageDto.getImageType())
                        .build();
                imageRepository.save(image);
            }
        }

        return auctionProducts;
    }

    @Transactional
    public void deleteAuction(Long auctionId, Long userId) {
        AuctionProductsEntity products = auctionProductsRepository.findByAuctionIdAndDeletedAtIsNull(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction Not Found or already deleted with ID: " + auctionId));

        Long productUserId = products.getUser().getUserId();

        if (productUserId == null || productUserId.longValue() != userId.longValue()) {
            throw new SecurityException("상품을 삭제할 권한이 없습니다. (판매자만 삭제 가능)");
        }

        products.setDeletedAt(LocalDateTime.now());
    }

    //  목록 조회 메서드
    @Transactional(readOnly = true)
    public PagingResponseDto<AuctionListResponseDto> getAuctionList(
            int page,
            int size,
            Integer categoryId,
            String searchKeyword,
            Boolean includeEnded,
            String sortBy,
            Integer minPrice,
            Integer maxPrice
    ) {

        // 1. 경매 상태 리스트 결정
        List<SellingStatus> statuses;
        if (Boolean.TRUE.equals(includeEnded)) {
            statuses = Arrays.asList(SellingStatus.COMPLETED, SellingStatus.PROGRESS, SellingStatus.FINISH);
        } else {
            statuses = List.of(SellingStatus.PROGRESS);
        }

        // 2. 정렬 기준(Sort)
        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "latest") {
            case "price_desc" -> Sort.by("currentPrice").descending();
            case "price_asc" -> Sort.by("currentPrice").ascending();
            case "end_time" -> Sort.by("endTime").ascending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Repository의 새로운 쿼리 메서드 호출
        Page<AuctionProductsEntity> auctionPage = auctionProductsRepository.findFilteredAuctionsByStatus(
                categoryId,
                searchKeyword,
                statuses,
                minPrice,
                maxPrice,
                pageable
        );

        // 4. DTO
        List<AuctionListResponseDto> dtoList = auctionPage.getContent().stream()
                .map(product -> {
                    // 찜 개수 조회
                    Integer wishCount = wishlistRepository.countByAuction(product);
                    // 메인 이미지
                    String mainImageUrl = imageRepository.findMainImageUrl(product.getAuctionId())
                            .orElse("default_product.png");

                    return AuctionListResponseDto.builder()
                            .auctionId(product.getAuctionId())
                            .title(product.getTitle())
                            .currentPrice(product.getCurrentPrice())
                            .endTime(product.getEndTime())
                            .sellingStatus(calculateSellingStatus(product))
                            .categoryName(product.getCategory().getCategoryName())
                            .sellerNickname(product.getUser().getNickname())
                            .mainImageUrl(mainImageUrl)
                            .wishCount(wishCount)
                            .build();
                })
                .toList();

        // 5. 페이징 응답 DTO 생성 (기존 로직 유지)
        return PagingResponseDto.<AuctionListResponseDto>builder()
                .data(dtoList)
                .totalPages(auctionPage.getTotalPages())
                .totalElements(auctionPage.getTotalElements())
                .currentPage(auctionPage.getNumber())
                .pageSize(auctionPage.getSize())
                .isFirst(auctionPage.isFirst())
                .isLast(auctionPage.isLast())
                .build();
    }

    //  경매 상태
    public String calculateSellingStatus(AuctionProductsEntity product) {
        return switch (product.getSellingStatus()) {
            case PROGRESS -> {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(product.getStartTime())) {
                    yield "시작"; // 시작 전
                } else if (now.isAfter(product.getEndTime())) {
                    yield "종료";
                } else {
                    yield "진행 중"; // 판매 중
                }
            }
            case SALE -> "시작";
            case COMPLETED -> "거래 완료";
            case FINISH -> "종료";
        };
    }

    //  경매물품 상세 조회
    @Transactional(readOnly = true)
    public AuctionFindDto getAuctionFind(Long auctionId, Long userId) {
        AuctionProductsEntity products = auctionProductsRepository.findByIdWithDetails(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction Not Found with ID: " + auctionId));

        List<ImageDto> imageDtos = products.getImages()
                .stream()
                .map(imageEntity -> ImageDto.builder()
                        .imageUrl(imageEntity.getImageUrl())
                        .imageType(imageEntity.getImageType())
                        .build())
                .collect(Collectors.toList());

        String sellingStatus = calculateSellingStatus(products);

        // 찜 개수
        Integer wishCount = wishlistRepository.countByAuction(products);

        // TODO: 임시온도 설정
        final Double DEFAULT_TEMP = 36.5;

        return AuctionFindDto.builder()
                .auctionId(products.getAuctionId())
                .title(products.getTitle())
                .description(products.getDescription())
                .currentPrice(products.getCurrentPrice())
                .minBidPrice(products.getMinBidPrice())
                .bidCount(products.getBidCount())
                .startTime(products.getStartTime())
                .createdAt(products.getCreatedAt())
                .endTime(products.getEndTime())
                .categoryId(products.getCategory().getCategoryId().longValue())
                .categoryName(products.getCategory().getCategoryName())
                .sellerId(products.getUser().getUserId())
                .sellerNickname(products.getUser().getNickname())
                .sellerProfileImageUrl(products.getUser().getProfileImageUrl())
                .images(imageDtos)
                .sellingStatus(sellingStatus)
                .wishCount(wishCount)
                .sellerTemperature(DEFAULT_TEMP) //
                .build();
    }

    @Transactional(readOnly = true)
    public AuctionProductsEntity findById(Long auctionId) {

        List<SellingStatus> allowedStatuses = List.of(SellingStatus.PROGRESS, SellingStatus.SALE);

        return auctionProductsRepository.findByAuctionIdAndSellingStatusIn(auctionId, allowedStatuses)
                .orElseThrow(() -> new RuntimeException("Auction product not found"));
    }

}