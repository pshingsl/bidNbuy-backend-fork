package com.bidnbuy.server.repository;

import com.bidnbuy.server.dto.NotificationResponse;
import com.bidnbuy.server.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 특정 유저의 삭제되지 않은 알림 목록
    List<NotificationEntity> findByUser_UserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    // 읽지 않은 알림만
    List<NotificationEntity> findByUserUserIdAndIsReadFalse(Long userId);

    // 읽음 처리
}
