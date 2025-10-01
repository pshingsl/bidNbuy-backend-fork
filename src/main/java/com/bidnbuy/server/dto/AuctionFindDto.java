package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
// 💡 클라이언트에게 상세 정보를 제공하기 위한 응답 DTO입니다.
public class AuctionFindDto {

    // 1. 상품 기본 정보
    private Long auctionId;
    private String title;
    private String description;

    // 2. 가격 및 입찰 정보
    private Integer startPrice;
    private Integer currentPrice; // 💡 입찰이 있을 경우 업데이트된 최고가
    private Integer minBidPrice;

    // 3. 시간 정보
    private LocalDateTime endTime;


    // 4. 판매자 정보 (다른 Entity에서 가져와야 함)
    private Long sellerId;
    private String sellerNickname; // 💡 User Entity에서 닉네임 등을 가져와 표시

    // 5. 카테고리 정보
    private Long categoryId;
    private String categoryName; // 💡 Category Entity에서 이름도 가져와 표시

    // 6. 이미지 정보 (등록 시의 ImageDto와 동일하거나 유사한 구조)
    private List<ImageDto> images;

    // 7. (선택) 경매 상태: "진행 중", "마감 임박", "종료" 등을 반환할 수 있음
    private String sellingStatus;
}
