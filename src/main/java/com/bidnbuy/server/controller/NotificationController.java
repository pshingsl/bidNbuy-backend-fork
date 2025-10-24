package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.NotificationResponse;
import com.bidnbuy.server.entity.NotificationEntity;
import com.bidnbuy.server.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserNotificationService userNotificationService;

    // 알림 전체 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Long userId) {
        userNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    // 알림 목록 전체 조회
    @GetMapping
    public List<NotificationResponse> getUserNotifications(@AuthenticationPrincipal Long userId) {
        return userNotificationService.getUserNotifications(userId);
    }

    // 개별 알림 삭제
    @DeleteMapping("/{id}")
    public Map<String, String> deleteNotification(@PathVariable Long id) {
        userNotificationService.deleteNotification(id);
        return Map.of("message", "알림이 삭제되었습니다");
    }

    // 전체 알림 삭제
    @DeleteMapping
    public Map<String, String> deleteAllNotifications(@AuthenticationPrincipal Long userId) {
        userNotificationService.deleteAllNotifications(userId);
        return Map.of("message", "전체 알림이 삭제되었습니다");
    }
}
