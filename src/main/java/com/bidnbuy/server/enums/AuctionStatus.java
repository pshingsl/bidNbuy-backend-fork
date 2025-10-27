package com.bidnbuy.server.enums;

import lombok.Getter;

@Getter
public enum AuctionStatus {
    // 경매 진행 상태
    PROGRESS,       // 진행 중
    FINISHED,       // 경매 마감 (낙찰자 결정)
    FAIL,           // 유찰 (낙찰자 없음)

    // 결제/거래 상태
    PAYMENT_PENDING, // 결제 대기 중 (낙찰 후)
    PAYMENT_COMPLETED, // 결제 완료
    PAYMENT_CANCELED, // 결제 취소

    // 배송/처리 상태 (필요 시)
    SHIPPING_PREPARING, // 배송 준비 중
    SHIPPING_IN_PROGRESS, // 배송 중
    TRADE_COMPLETED, // 최종 거래 완료 (수령 확정)

    // 관리/시스템 상태
    CANCELLED_BY_SELLER, // 판매자에 의한 취소
    CANCELLED_BY_SYSTEM  // 시스템에 의한 취소 (예: 기한 초과)
}
