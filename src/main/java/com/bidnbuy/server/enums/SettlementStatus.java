package com.bidnbuy.server.enums;

public enum SettlementStatus {
    WAITING,   // 결제 완료 후 정산 대기
    DONE,      // 정산 완료
    HOLD       // 분쟁/환불 등으로 정산 보류
}
