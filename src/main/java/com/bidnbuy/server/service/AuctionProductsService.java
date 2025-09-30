package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.CreateAuctionDto;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuctionProductsService {
    @Autowired
    private AuctionProductsRepository auctionProductsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    // create
    @Transactional
    public AuctionProductsEntity create(CreateAuctionDto dto, Long userId) {

        //
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("등록자(User)를 찾을 수 없습니다. ID: " + userId));

        // 1-2. 카테고리 조회 및 유효성 검증
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

        return auctionProductsRepository.save(auctionProduct);
    }


    /*
    * CreateAuctionDto를 부른다.
    * -> 위 캡션 이미지에서 데이터 등록
    * -> 지금은 이미지, 카테고리를 구현 안해서 제외하고 구현
    * -> 요구사항 명세서에서 입력을 안하면 해당 미입력 부분 에러 발생
    * */
}