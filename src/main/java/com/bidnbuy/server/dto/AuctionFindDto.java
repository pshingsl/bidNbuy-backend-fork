package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.WishlistEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 경매 상품 상세 조회
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionFindDto {
    // 1. 상품 기본 정보
    private List<ImageDto> images; // 이미지
    private Long auctionId;        // 상품 아이디
    private String title;          // 제품명
    private String description;    // 제품 설명

    // 2. 카테고리 정보
    private Integer categoryId;
    private String categoryName; // 카테고리 이름

    // 3. 가격 및 입찰 정보
    private Integer currentPrice; // 현재가
    private Integer minBidPrice;
    private Integer bidCount; // 입찰 횟수

    // 4. 시간 정보
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private LocalDateTime endTime;
    private LocalDateTime updatedAt;

    // 5. 판매자 정보 (다른 Entity에서 가져와야 함)
    private Long sellerId;
    private String sellerNickname; // 판매자
    private String sellerProfileImageUrl;
    private Double sellerTemperature;


    // 6. (선택) 경매 상태: "진행 중", "마감 임박", "종료" 등을 반환할 수 있음
    private String sellingStatus;

    // 7. 찜 카운트
    private Integer wishCount;

    public AuctionFindDto(AuctionProductsEntity entity) {
//        this.auctionId = entity.getAuctionId();
//        this.title = entity.getTitle();
//        this.description = entity.getDescription();
//        this.categoryId =  entity.getCategory().getCategoryId();
//        this.categoryName =entity.getCategory().getCategoryName();
//        this.currentPrice = entity.getCurrentPrice();
//        this.minBidPrice = entity.getMinBidPrice();
//        this.bidCount = entity.getBidCount();
//        this.startTime = entity.getStartTime();
//        this.endTime = entity.getEndTime();
//        this.createdAt = entity.getCreatedAt();
//        this.sellerId = entity.getUser().getUserId();
//        this.sellerNickname = entity.getUser().getNickname();
//       // this.wishCount = entity.getWishes().size();
//        this.sellerProfileImageUrl = entity.getUser().getProfileImageUrl();
//
//        this.images = entity.getImages().stream()
//                .map(ImageDto::new) // ImageDto에 정적 팩토리 메서드 from(ImageEntity)가 있다고 가정
//                .collect(Collectors.toList());
   }
}
