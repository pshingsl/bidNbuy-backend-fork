package com.bidnbuy.server.controller;

import com.bidnbuy.server.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // 구매자가 거래 확정 버튼을 누르는 API
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmSettlement(@PathVariable Long orderId, @AuthenticationPrincipal Long userId) {
        settlementService.confirmSettlement(orderId, userId);
        return ResponseEntity.ok("정산 완료되었습니다.");
    }
}