package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.CategoryEntity;
import com.bidnbuy.server.entity.ImageEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.AuctionStatus;
import com.bidnbuy.server.enums.ImageType;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionProductsService {

    // 불변성을 보장하기 위해 final로 사용
    private final AuctionProductsRepository auctionProductsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final WishlistRepository wishlistRepository;
    private final AuctionHistoryService auctionHistoryService;
    // 💡 이미지 처리를 위해 ImageService 주입
    private final ImageService imageService;

    @Transactional
    public AuctionProductsEntity create(Long userId, CreateAuctionDto dto, List<MultipartFile> imageFiles) {

        // 1. 기본 데이터 유효성 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 존재하지 않습니다!"));

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다!"));

        // 2. 물품 등록 생성 및 기본 정보 설정
        AuctionProductsEntity auctionProducts =  CreateAuctionDto.toEntity(dto);
        auctionProducts.setUser(user);
        auctionProducts.setCategory(category);
        auctionProducts.setCurrentPrice(dto.getStartPrice());
        auctionProducts.setSellingStatus(SellingStatus.PROGRESS);
        auctionProducts.setBidCount(0);

        // 3. 물품 저장 (ID를 획득하여 이미지 경로에 사용하기 위해 먼저 저장)
        AuctionProductsEntity savedProducts = auctionProductsRepository.save(auctionProducts);

        // 4. 이미지 파일 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<ImageEntity> imageEntities = new ArrayList<>();

            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);

                // 💡 ImageService 호출: 파일을 로컬에 저장하고 영구 URL을 반환받음
                String permanentUrl = imageService.uploadAuctionImage(savedProducts.getAuctionId(), file);

                // 💡 첫 번째 이미지를 MAIN 타입으로 지정, 나머지는 PRODUCT
                ImageType imageType = (i == 0) ? ImageType.MAIN : ImageType.PRODUCT;

                // 💡 ImageEntity 생성 및 영구 URL 저장
                ImageEntity imageEntity = ImageEntity.builder()
                        .auctionProduct(savedProducts)
                        .user(null)
                        .imageUrl(permanentUrl) // 영구 URL 저장
                        .imageType(imageType)
                        .build();

                imageEntities.add(imageEntity);
            }

            // Image Entity들을 DB에 일괄 저장
            imageRepository.saveAll(imageEntities);
            savedProducts.setImages(imageEntities);

            // 경매 상태 기록
            auctionHistoryService.recordStatusChange(
                    savedProducts.getAuctionId(),
                    AuctionStatus.PROGRESS
            );
        }

        return  savedProducts;
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
        // 💡 추가 개선 사항: 파일 저장소가 로컬/S3인 경우, 여기서 ImageService를 통해 실제 파일 삭제 로직을 호출해야 함.
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
                    String mainImageUrl = imageRepository.findFirstImageUrlByAuctionId(product.getAuctionId())
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

        // 5. 페이징 응답 DTO 생성
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

    //  경매 상태 계산 메서드
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

        // 이미지 DTO 변환 시 DB에 저장된 영구 URL 사용
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
                .categoryId(products.getCategory().getCategoryId())
                .categoryName(products.getCategory().getCategoryName())
                .sellerId(products.getUser().getUserId())
                .sellerNickname(products.getUser().getNickname())
                .sellerProfileImageUrl(products.getUser().getProfileImageUrl())
                .images(imageDtos)
                .sellingStatus(sellingStatus)
                .wishCount(wishCount)
                .sellerTemperature(DEFAULT_TEMP)
                .build();
    }

    @Transactional(readOnly = true)
    public AuctionProductsEntity findById(Long auctionId) {

        List<SellingStatus> allowedStatuses = List.of(SellingStatus.PROGRESS, SellingStatus.SALE);

        return auctionProductsRepository.findByAuctionIdAndSellingStatusIn(auctionId, allowedStatuses)
                .orElseThrow(() -> new RuntimeException("Auction product not found"));
    }
}