package com.bidnbuy.server.enums;

public enum ResultStatus {
    SUCCESS_PENDING_PAYMENT,    // 낙찰 후 결제 대기중(진행 중)
    SUCCESS_COMPLETED,          // 낙찰 후 거래 완료(완료)
    FAILURE,                    // 유찰 (낙찰자가 없어 종료)
    CANCELED;                   // 취소
}
