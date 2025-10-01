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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // create
    @Transactional
    public AuctionProductsEntity create(CreateAuctionDto dto, List<ImageDto> images, Long userId) {

        // 유저 아이디 유효성 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("등록자(User)를 찾을 수 없습니다. ID: " + userId));

        // 카테고리 조회 및 유효성 검증
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다. ID: " + dto.getCategoryId()));

        AuctionProductsEntity auctionProduct = AuctionProductsEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startPrice(dto.getStartPrice())
                .currentPrice(dto.getStartPrice())
                .minBidPrice(dto.getMinBidPrice())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .sellingStatus(SellingStatus.SALE)
                .category(category)
                .build();

        auctionProduct.setUser(user);

        // DB에 저장하여 auctionId  확보
        AuctionProductsEntity auctionProducts = auctionProductsRepository.save(auctionProduct);

        // 이미지 검증
        if(images == null || images.size() == 0 ||images.size() > 10) {
            throw new IllegalArgumentException("이미지는 1장 이상 10장 이하로 등록해야 합니다.");
        }

        for(ImageDto imageDto:images) {
            ImageEntity imageEntity = ImageEntity.builder()
                    .imageUrl(imageDto.getImageUrl())
                    .imageType(imageDto.getImageType())
                    .auctionProduct(auctionProduct)
                    .build();

            imageRepository.save(imageEntity);
        }
        return auctionProducts;
    }

    // 조회
    @Transactional(readOnly = true)
    public PagingResponseDto<AuctionListResponseDto> getAuctionList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<AuctionProductsEntity> auctionPage = auctionProductsRepository.findRunningAuctionsWithDetails(pageable);

        List<AuctionListResponseDto> dtoList = auctionPage.getContent().stream()
                .map(product ->{
                    String mainImageUrl = imageRepository.findMainImageUrl(product.getAuctionId())
                            .orElse("default_product.png"); // 이미지가 없을 때 기본값 처리

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

    private String calculateSellingStatus(AuctionProductsEntity product) {
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(product.getStartTime())){
            return "시작";
    }else if (now.isAfter(product.getEndTime())) {
            return "종료";
        } else{
            return "진행 중";
        }
}

    @Transactional(readOnly = true)
    public AuctionFindDto getAuctionFind(Long auctionId, Long userId) {
        AuctionProductsEntity products = auctionProductsRepository.findByIdWithDetails(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction Not Found with ID: " + auctionId));

        List<ImageDto> imageDtos = imageRepository.findAllByAuctionProduct_AuctionId(auctionId)
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
                .sellerId(products.getUser().getUserId())
                .sellerNickname(products.getUser().getNickname())
                .images(imageDtos)
                .build();
    }
}