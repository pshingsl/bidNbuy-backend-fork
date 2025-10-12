package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.AuctionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuctionHistoryDto {
    private AuctionStatus previousStatus; // 이전 상태 (Enum)
    private AuctionStatus newStatus;      // 새로운 상태 (Enum)
    private String statusDescription;     // 새로운 상태에 대한 사용자 친화적인 설명 (예: "낙찰 후 결제 대기 시작")
    private LocalDateTime bidTime;      // 변경 시점
}
