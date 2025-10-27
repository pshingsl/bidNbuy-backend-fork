package com.bidnbuy.server.enums;

import lombok.Getter;

@Getter
public enum ResultStatus {
    SUCCESS_PENDING_PAYMENT,    // 낙찰 후 결제 대기중(진행 중)
    SUCCESS_PAID,               // 낙찰 후 결제 완료, 거래 진행 중 (Order 상태: PAID)
    SUCCESS_COMPLETED,          // 낙찰 후 거래 완료(완료)
    FAILURE,                    // 유찰 (낙찰자가 없어 종료)
    CANCELED;                   // 취소
}