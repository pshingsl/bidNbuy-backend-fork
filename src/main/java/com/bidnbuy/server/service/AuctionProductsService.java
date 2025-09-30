package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.CreateAuctionDto;
import com.bidnbuy.server.dto.ImageDto;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuctionProductsService {
    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired ImageService imageService;
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
        return auctionProducts;
    }
}