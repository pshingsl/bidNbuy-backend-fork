package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Getter
@Builder
@EqualsAndHashCode(of = {"auctionId"})
public class AuctionSalesHistoryDto {
    private Long auctionId;
    private String title;
    private String itemImageUrl;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer finalPrice;

    // 낙찰자 정보
    private String winnerNickname;
    private String statusText;

    // 여기 추가
    private String recipientName;   // 받는 사람 이름
    private String phoneNumber;     // 전화번호
    private String zonecode;        // 우편번호
    private String address;         // 기본 주소
    private String detailAddress;   // 상세 주소
}