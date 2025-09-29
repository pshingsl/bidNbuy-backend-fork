package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.CreateAuctionDTO;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuctionProductsService {
    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionProductsRepository auctionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    // create
    public Long createAuctionProduct(CreateAuctionDTO dto, Long userId) {

        // 1. 필수 연관 엔티티 조회 (유효성 검사)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        // TODO: 아직 카테고리 구현이 안돼 하드코딩 처리
        Long TEMPORARY_CATEGORY_ID = 1L;
        CategoryEntity category = categoryRepository.findById(TEMPORARY_CATEGORY_ID)
                .orElseThrow(() -> new EntityNotFoundException("임시 카테고리 ID " + TEMPORARY_CATEGORY_ID + "를 찾을 수 없습니다."));

        // 💡 TODO: 아직 찜목록 구현이 안돼 하드코딩 처리
        final Long TEMPORARY_WISHLIST_ID = 1L;
        WishlistEntity wishlist = wishlistRepository.findById(TEMPORARY_WISHLIST_ID)
                .orElseThrow(() -> new EntityNotFoundException("임시 위시리스트 ID " + TEMPORARY_WISHLIST_ID + "를 찾을 수 없습니다."));

        // 2. DTO -> AuctionProductsEntity 변환 및 저장
        AuctionProductsEntity auctionProduct = AuctionProductsEntity.builder()
                .user(user)
                .category(category) // 카테고리 추가
                // DTO 필드명 수정 (start_price -> startPrice 등)
                .title(dto.getTitle())
                .wishlist(wishlist)
                .description(dto.getDescription())
                .startPrice(dto.getStart_price())
                .minBidPrice(dto.getMin_bid_price())
                .startTime(dto.getStart_time())
                .endTime(dto.getEnd_time())
                .sellingStatus(SellingStatus.SALE) // 기본값 설정
                .currentPrice(dto.getStart_price()) // 등록 시점에는 시작가로 설정
                .deletedAt(LocalDateTime.of(1970, 1, 1, 0, 0, 0))
                .build();

        AuctionProductsEntity savedAuction = auctionRepository.save(auctionProduct);

        return savedAuction.getAuctionId();
    }
}