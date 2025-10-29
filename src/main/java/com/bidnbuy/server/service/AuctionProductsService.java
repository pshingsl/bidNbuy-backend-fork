package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.AuctionStatus;
import com.bidnbuy.server.enums.ImageType;
import com.bidnbuy.server.enums.NotificationType;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import com.sun.tools.jconsole.JConsoleContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionProductsService {

    // 불변성을 보장하기 위해 final로 사용
    private final AuctionProductsRepository auctionProductsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final WishlistRepository wishlistRepository;
    private final AuctionHistoryService auctionHistoryService;
    private final ImageService imageService;
    private final UserNotificationService userNotificationService;

    @Transactional
    public AuctionProductsEntity create(Long userId, CreateAuctionDto dto, List<MultipartFile> imageFiles) {

        // 1. 기본 데이터 유효성 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 존재하지 않습니다!"));

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다!"));

        // 2. 물품 등록 생성 및 기본 정보 설정
        AuctionProductsEntity auctionProducts = CreateAuctionDto.toEntity(dto);
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

                String permanentUrl = imageService.uploadAuctionImage(savedProducts.getAuctionId(), file);

                //  첫 번째 이미지를 MAIN 타입으로 지정, 나머지는 PRODUCT
                ImageType imageType = (i == 0) ? ImageType.MAIN : ImageType.PRODUCT;

                // ImageEntity 생성 및 영구 URL 저장
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

        // 알림 추가 - kgb
        userNotificationService.createNotification(userId, NotificationType.ALERT, "경매가 등록되었습니다.");

        return savedProducts;
    }

    // 전체 리스트 출력
    @Transactional(readOnly = true)
    public PagingResponseDto<AuctionListResponseDto> getAllAuctions(
            Long userId,
            int page,
            int size,
            Integer minPrice,
            Integer maxPrice,
            String sortBy,
            Boolean includeEnded,
            String searchKeyword,
            Long mainCategoryId,
            Long subCategoryId,
            String userEmail
    ) {

        // 상태 리스트 결정, 정렬, 페이지
        List<SellingStatus> statuses = getFilterStatuses(includeEnded);
        Sort sort = getSortCriteria(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Repository 호출: 수정한 가격/상태 필터링 메서드 호출
        // 이메일로 특정 유저 경매 상품 조회 추가
        Page<AuctionProductsEntity> auctionPage;
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            auctionPage = auctionProductsRepository.findByUserEmailAndStatuses(
                    userEmail, statuses, searchKeyword, mainCategoryId, subCategoryId, minPrice, maxPrice, pageable
            );
        } else {
            // 기존 필터링
            auctionPage = auctionProductsRepository.findDynamicFilteredAuctions(
                    searchKeyword,
                    mainCategoryId,
                    subCategoryId,
                    minPrice,
                    maxPrice,
                    statuses,
                    pageable
            );
        }

        // 3. DTO 변환 및 반환
        return buildPagingResponse(auctionPage, userId);
    }
//
//    @Transactional(readOnly = true)
//    public List<AuctionListResponseDto> getMyAuctionListSimple(Long userId) {
//
//        // 1. 사용자 엔티티 조회
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 사용자 존재하지 않습니다."));
//
//        // 2. Repository를 통해 해당 사용자가 작성한 모든 상품을 조회
//        List<AuctionProductsEntity> myProducts = auctionProductsRepository.findProductsByUserEagerly(user);
//
//        // 3. DTO로 변환
//        return myProducts.stream()
//                .map(product -> {
//                    String mainImageUrl = imageRepository.findFirstImageUrlByAuctionId(product.getAuctionId())
//                            .orElse("default_product.png");
//
//                    return AuctionListResponseDto.builder()
//                            .auctionId(product.getAuctionId())
//                            .title(product.getTitle())
//                            .currentPrice(product.getCurrentPrice())
//                            .createdAt(product.getCreatedAt())
//                            .endTime(product.getEndTime())
//                            .sellingStatus(calculateSellingStatus(product))
//                            //.categoryName(product.getCategory().getCategoryName())
//                            .sellerId(product.getUser().getUserId())
//                            .sellerNickname(product.getUser().getNickname())
//                            .mainImageUrl(mainImageUrl)
//                            .wishCount(wishlistRepository.countByAuction(product))
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }

    // 상세조회
    @Transactional(readOnly = true)
    public AuctionFindDto getAuctionFind(Long auctionId, Long userId) {
        // auctionId로 상품을 조회하며, 필요한 연관 엔티티(user, category, images)를 JOIN FETCH로 함께 가져옵니다.
        AuctionProductsEntity products = auctionProductsRepository.findByIdWithDetails(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction Not Found with ID: " + auctionId));

        // 로그인한 유저가 찜했는지 확인
        boolean liked = false;
        if(userId != null) {
            liked = wishlistRepository.existsByUser_UserIdAndAuction_AuctionId(userId, auctionId);
        }

        // 이미지 DTO 변환 시 DB에 저장된 영구 URL 사용
        List<ImageDto> imageDtos = products.getImages()
                .stream()
                .map(imageEntity -> ImageDto.builder()
                        .imageUrl(imageEntity.getImageUrl())
                        .imageType(imageEntity.getImageType())
                        .build())
                .collect(Collectors.toList());

        // 현재 시점을 기준으로 경매 상태 계산
        String sellingStatus = calculateSellingStatus(products);

        // 찜 개수 조회
        Integer wishCount = wishlistRepository.countByAuction(products);

        // 판매자 온도 (임의 값 또는 실제 로직을 통해 가져와야 함)
        Double sellerTemperature = products.getUser().getUserTemperature();

        // 대소분류 분리
        CategoryEntity subCategoryEntity = products.getCategory();
        CategoryEntity mainCategoryEntity = subCategoryEntity.getParent();

        // 배열로 처리해ㅓ 문제
        String mainCategory = "";
        String subCategory = subCategoryEntity.getCategoryName();

        if (mainCategoryEntity != null) {
            String fullMainName = mainCategoryEntity.getCategoryName();

            // DB에 "대분류/소분류" 형태로 저장되어 있다면 첫 번째 파트만 사용
            mainCategory = fullMainName;

        } else {
            // 부모가 없다면 (스스로 대분류라면), 현재 이름을 mainCategory에 넣고 subCategory는 비움
            mainCategory = subCategoryEntity.getCategoryName();
            subCategory = null;

        }

        boolean liked = false;
        if (userId != null) {
            liked = wishlistRepository.existsByUser_UserIdAndAuction_AuctionId(userId, products.getAuctionId());
        }

        // 최종 DTO 빌드 및 반환
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
                .categoryMain(mainCategory)
                .categorySub(subCategory)
                .sellerId(products.getUser().getUserId())
                .sellerNickname(products.getUser().getNickname())
                .sellerProfileImageUrl(products.getUser().getProfileImageUrl())
                .images(imageDtos)
                .sellingStatus(sellingStatus)
                .wishCount(wishCount)
                .liked(liked)
                .sellerTemperature(sellerTemperature)
                .build();
    }

    // 삭제
    @Transactional
    public void deleteAuction(Long auctionId, Long userId) {
        AuctionProductsEntity products = auctionProductsRepository.findByAuctionIdAndDeletedAtIsNull(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction Not Found or already deleted with ID: " + auctionId));

        Long productUserId = products.getUser().getUserId();

        if (productUserId == null || productUserId.longValue() != userId.longValue()) {
            throw new SecurityException("상품을 삭제할 권한이 없습니다. (판매자만 삭제 가능)");
        }

        products.setDeletedAt(LocalDateTime.now());

        auctionProductsRepository.save(products);
    }

    // 관리자용 삭제
    @Transactional
    public void deleteAuctionByAdmin(Long auctionId) {
        AuctionProductsEntity products = auctionProductsRepository.findByAuctionIdAndDeletedAtIsNull(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매를 찾을 수 없습니다: " + auctionId));

        products.setDeletedAt(LocalDateTime.now());
        auctionProductsRepository.save(products);

        log.info("관리자에 의해 경매 삭제: auctionId={}, title={}", auctionId, products.getTitle());
    }


    // 경매 상태 리스트 결정 메서드
    private List<SellingStatus> getFilterStatuses(Boolean includeEnded) {
        if (Boolean.TRUE.equals(includeEnded)) {
            // 종료, 완료된 상품까지 모두 포함
            return Arrays.asList(SellingStatus.BEFORE, SellingStatus.SALE, SellingStatus.PROGRESS, SellingStatus.FINISH, SellingStatus.COMPLETED);
        } else {
            // 진행 중이거나 예정인 상품만
            return Arrays.asList(SellingStatus.PROGRESS, SellingStatus.SALE, SellingStatus.BEFORE);
        }
    }

    // 정렬 기준 설정 메서드
    private Sort getSortCriteria(String sortBy) {
        return switch (sortBy != null ? sortBy.toLowerCase() : "latest") {
            case "price_desc" -> Sort.by("currentPrice").descending();
            case "price_asc" -> Sort.by("currentPrice").ascending();
            case "end_time" -> Sort.by("endTime").ascending();
            default -> Sort.by("createdAt").descending(); // 기본: 최신순
        };
    }

    // DTO 매핑 유틸리티 (목록용) - 이 메서드는 실제 엔티티를 DTO로 변환하는 상세 로직이 필요합니다.
    private AuctionListResponseDto mapToAuctionListResponseDto(AuctionProductsEntity product, Long userId) {
        // 이 부분은 기존에 구현했던 AuctionListResponseDto 매핑 로직을 사용해야 합니다.
        Integer wishCount = wishlistRepository.countByAuction(product);
        String mainImageUrl = imageRepository.findFirstImageUrlByAuctionId(product.getAuctionId())
                .orElse("default_product.png");

        boolean liked = false;
        if (userId != null) {
            liked = wishlistRepository.existsByUser_UserIdAndAuction_AuctionId(userId, product.getAuctionId())
        }

        return AuctionListResponseDto.builder()
                .auctionId(product.getAuctionId())
                .title(product.getTitle())
                .currentPrice(product.getCurrentPrice())
                .createdAt(product.getCreatedAt())
                .endTime(product.getEndTime())
                .sellingStatus(calculateSellingStatus(product)) // calculateSellingStatus도 클래스에 있어야 합니다.
                //  .categoryName(product.getCategory().getCategoryName())
                .sellerId(product.getUser().getUserId())
                .sellerNickname(product.getUser().getNickname())
                .mainImageUrl(mainImageUrl)
                .wishCount(wishCount)
                .liked(liked)
                .build();
    }

    // 페이징 응답 DTO 빌더 유틸리티
    private PagingResponseDto<AuctionListResponseDto> buildPagingResponse(Page<AuctionProductsEntity> auctionPage, Long userId) {
        List<AuctionListResponseDto> dtoList = auctionPage.getContent().stream()
                .map(product -> mapToAuctionListResponseDto(product, userId))
                .toList();

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
                    yield "시작전"; // 시작 전
                } else if (now.isAfter(product.getEndTime())) {
                    yield "종료";
                } else {
                    yield "진행중"; // 판매 중
                }
            }
            case SALE -> "진행중";
            case BEFORE -> "시작전";
            case COMPLETED -> "거래완료"; // 거래완료
            case FINISH -> "종료";
        };
    }

    @Transactional(readOnly = true)
    public AuctionProductsEntity findById(Long auctionId) {

        List<SellingStatus> allowedStatuses = List.of(SellingStatus.PROGRESS, SellingStatus.SALE,
                SellingStatus.FINISH);

        return auctionProductsRepository.findByAuctionIdAndSellingStatusIn(auctionId, allowedStatuses)
                .orElseThrow(() -> new RuntimeException("Auction product not found"));
    }

    //채팅연결
    @Transactional(readOnly = true)
    public Optional<AuctionProductsEntity> findByIdAnyway(Long auctionId) {
        return auctionProductsRepository.findById(auctionId);
    }
}