package com.bidnbuy.server.enums;

// 구매내역, 판매내역에 사용할 타입
public enum TradeFilterStatus {
    ALL,        // 전체
    ONGOING,    // 진행 중  (결제 대기 등)
    COMPLETED,  // 완료(거래 완료)
    CANCELLED    // 취소/실패(유찰, 거래. 취소)
}
