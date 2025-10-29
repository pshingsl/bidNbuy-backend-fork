package com.bidnbuy.server.enums;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    WAITING,  // 결제 완료 후 대기
    DONE,     // 거래 완료 → 정산 완료
    HOLD      // 정산 보류 (필요 시)
}
