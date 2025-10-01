// com.bidnbuy.server.service.AuctionProductsService.java

package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // 💡 Sort import 유지
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays; // 💡 Arrays import 유지
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuctionProductsService {
    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    // create 메서드는 그대로 유지
    @Transactional
    public AuctionProductsEntity create(CreateAuctionDto dto, List<ImageDto> images, Long userId) {
        // ... (기존 create 로직 유지)

        // 유저 아이디 유효성 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("등록자(User)를 찾을 수 없습니다. ID: " + userId));

        // 카테고리 조회 및 유효성 검증
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리 ID입니다."));

        // AuctionProductsEntity 생성
        AuctionProductsEntity auctionProducts = AuctionProductsEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startPrice(dto.getStartPrice())
                .currentPrice(dto.getStartPrice()) // 시작 가격으로 현재 가격 초기화
                .minBidPrice(dto.getMinBidPrice())
                .sellingStatus(SellingStatus.SALE)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();

        // 연관 관계 설정
        auctionProducts.setUser(user);
        auctionProducts.setCategory(category);

        // 저장
        auctionProductsRepository.save(auctionProducts);

        // 이미지 저장 로직 (ImageService를 사용하는 것이 좋으나, 현재 코드 구조 유지)
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

    // 💡 목록 조회 메서드 확장 및 수정 (가격 범위 필터링 적용)
    @Transactional(readOnly = true)
    public PagingResponseDto<AuctionListResponseDto> getAuctionList(
            int page,
            int size,
            Integer categoryId,
            String searchKeyword,
            Boolean includeEnded,
            String sortBy,
            // 💡 [변경] 가격 범위 필터링 파라미터 추가
            Integer minPrice,
            Integer maxPrice
    ) {

        // 1. 경매 상태 리스트 결정 (기존 로직 유지)
        List<SellingStatus> statuses;
        if (Boolean.TRUE.equals(includeEnded)) {
            statuses = Arrays.asList(SellingStatus.SALE, SellingStatus.COMPLETED, SellingStatus.CANCEL);
        } else {
            statuses = List.of(SellingStatus.SALE);
        }

        // 2. 정렬 기준(Sort) 설정 (기존 로직 유지)
        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "latest") {
            case "price" -> Sort.by("currentPrice").descending();
            case "end_time" -> Sort.by("endTime").ascending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sort);

        // 3. Repository의 새로운 쿼리 메서드 호출
        // (AuctionProductsRepository의 findFilteredAuctionsByStatus 메서드 시그니처가 변경되었습니다.)
        Page<AuctionProductsEntity> auctionPage = auctionProductsRepository.findFilteredAuctionsByStatus(
                categoryId,
                searchKeyword,
                statuses,
                // 💡 [변경] 가격 파라미터 전달
                minPrice,
                maxPrice,
                pageable
        );

        // 4. DTO 매핑 (기존 로직 유지)
        List<AuctionListResponseDto> dtoList = auctionPage.getContent().stream()
                .map(product ->{
                    String mainImageUrl = imageRepository.findMainImageUrl(product.getAuctionId())
                            .orElse("default_product.png");

                    return AuctionListResponseDto.builder()
                            .auctionId(product.getAuctionId())
                            .title(product.getTitle())
                            .currentPrice(product.getCurrentPrice())
                            .endTime(product.getEndTime())
                            .sellingStatus(calculateSellingStatus(product))
                            .categoryName(product.getCategory().getCategoryName())
                            .mainImageUrl(mainImageUrl)
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

    // 💡 calculateSellingStatus 메서드 유지
    private String calculateSellingStatus(AuctionProductsEntity product) {
        // AuctionProductsEntity의 SellingStatus Enum 값을 한글로 변환하여 반환
        return switch (product.getSellingStatus()) {
            case SALE -> {
                LocalDateTime now = LocalDateTime.now();
                if(now.isBefore(product.getStartTime())){
                    yield "시작 예정"; // 시작 전
                } else if(now.isAfter(product.getEndTime())) {
                    yield "종료"; // DB 상태와 별개로 시간이 지난 경우 (COMPLETED 또는 CANCEL로 업데이트 되어야 하지만 안전 장치)
                } else {
                    yield "진행 중"; // 판매 중
                }
            }
            case COMPLETED -> "거래 완료";
            case CANCEL -> "취소/삭제";
        };
    }

    // 💡 getAuctionFind 메서드 유지
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

        return AuctionFindDto.builder()
                .auctionId(products.getAuctionId())
                .title(products.getTitle())
                .description(products.getDescription())
                .startPrice(products.getStartPrice())
                .currentPrice(products.getCurrentPrice())
                .minBidPrice(products.getMinBidPrice())
                .endTime(products.getEndTime())

                .categoryId(products.getCategory().getCategoryId().longValue())
                .categoryName(products.getCategory().getCategoryName())

                .sellerId(products.getUser().getUserId())
                .sellerNickname(products.getUser().getNickname())
                .images(imageDtos)
                .sellingStatus(sellingStatus)
                .build();
    }
}