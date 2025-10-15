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
    // private String categoryName; // 카테고리 이름
    private String categoryMain;
    private String categorySub;

    // 3. 가격 및 입찰 정보
    private Integer currentPrice; // 현재가
    private Integer minBidPrice;
    private Integer bidCount; // 입찰 횟수

    // 4. 시간 정보
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private LocalDateTime endTime;

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
        this.auctionId = entity.getAuctionId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();

        // 💡 카테고리 정보 매핑 (CategoryEntity에 Main/Sub 필드가 없다고 가정)
        this.categoryId = entity.getCategory().getCategoryId();
        String fullCategoryName = entity.getCategory().getCategoryName();

        String[] parts = fullCategoryName.split("/");

        // 메인 카테고리 할당
        if (parts.length >= 1) {
            this.categoryMain = parts[0].trim();
        } else {
            this.categoryMain = fullCategoryName.trim(); // 분리할 수 없으면 전체 이름을 Main으로
        }

        // 서브 카테고리 할당
        if (parts.length >= 2) {
            this.categorySub = parts[1].trim();
        } else {
            this.categorySub = null; // 서브 카테고리가 없는 경우
        }


        // 가격 및 입찰 정보
        this.currentPrice = entity.getCurrentPrice();
        this.minBidPrice = entity.getMinBidPrice();
        this.bidCount = entity.getBidCount();

        // 시간 정보
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
        this.createdAt = entity.getCreatedAt();

        // 판매자 정보
        this.sellerId = entity.getUser().getUserId();
        this.sellerNickname = entity.getUser().getNickname();
        this.sellerProfileImageUrl = entity.getUser().getProfileImageUrl();

        // 이미지 정보
        this.images = entity.getImages().stream()
                .map(ImageDto::new)
                .collect(Collectors.toList());
   }
}
