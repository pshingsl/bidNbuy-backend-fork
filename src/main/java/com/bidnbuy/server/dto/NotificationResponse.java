package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.NotificationEntity;
import com.bidnbuy.server.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private NotificationType type;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // 엔티티 -> DTO 변환 메서드
    public static NotificationResponse fromEntity(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getNotificationId(),
                entity.getType(),
                entity.getContent(),
                entity.isRead(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }
}
