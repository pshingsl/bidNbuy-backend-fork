package com.bidnbuy.server.scheduler;

import com.bidnbuy.server.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoCancelScheduler {

    private final OrderService orderService;

    // 1시간마다 실행 (3600000ms)
    @Scheduled(fixedRate = 360000)
    public void runAutoCancel() {
        System.out.println("✅ 자동 취소 스케줄러 실행됨 - kgb");
        orderService.autoCancelExpiredOrders();
        System.out.println("✅ 자동 취소 스케줄러 완료됨 - kgb");
    }
}