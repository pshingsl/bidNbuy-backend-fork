package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.NotificationResponse;
import com.bidnbuy.server.entity.NotificationEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.NotificationType;
import com.bidnbuy.server.repository.UserNotificationRepository;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    // 알림 전체 읽음 처림
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    /**
     * 알림 생성 (이력 저장)
     */
    @Transactional
    public void createNotification(Long userId, NotificationType type, String content) {
        log.info("🔔 createNotification 호출됨 userId={}, type={}, message={}", userId, type, content);

        // 1. 유저 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("✅ 알림 대상 사용자 찾음: {}", user.getUserId());

        // 2. DB 저장
        NotificationEntity noti = NotificationEntity.builder()
                .user(user)
                .type(type)
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationEntity saved = notificationRepository.save(noti);

        // 3. FCM 전송 (토큰 없거나 실패해도 DB는 남음)
        // 🔔 타입별 타이틀 설정
        String title;
        switch (type) {
            case ALERT -> title = "🔔 일반 알림";
            case NOTICE -> title = "📢 공지사항";
            case WARN -> title = "⚠️ 경고";
            default -> title = "새 알림";
        }

        // ✅ FCM 전송
        try {
            fcmService.sendNotification(
                    userId,
                    title,                        // 타입별 타이틀 전달
                    content,                      // 본문 내용
                    type.name(),                  // 알림 타입
                    saved.getNotificationId(),    // 식별자
                    saved.getCreatedAt()          // 생성일자
            );
        } catch (Exception e) {
            log.warn("⚠️ FCM 전송 실패 (DB 저장은 완료됨): {}", e.getMessage());
        }
    }

    ;

    // ✅ 소프트 삭제 (개별)
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(noti -> {
            noti.setDeletedAt(LocalDateTime.now());
            notificationRepository.save(noti);
        });
    }

    // ✅ 소프트 삭제 (전체)
    @Transactional
    public void deleteAllNotifications(Long userId) {
        List<NotificationEntity> notis = notificationRepository.findByUser_UserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        notis.forEach(n -> n.setDeletedAt(LocalDateTime.now()));
        notificationRepository.saveAll(notis);
    }


    /**
     * 유저 알림 목록 전체 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUser_UserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(noti -> {
            noti.setRead(true);
            notificationRepository.save(noti);
        });
    }

    // 특정 유저에게 공지사항 알림 발송
    public NotificationEntity createNotice(Long userId, String content) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        NotificationEntity noti = NotificationEntity.builder()
                .user(user)
                .type(NotificationType.NOTICE)  // 공지사항 고정
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(noti);
    }

    // 전체 유저에게 공지사항 알림 발송
    public void createNoticeForAll(String content) {
        userRepository.findAll().forEach(user -> {
            NotificationEntity noti = NotificationEntity.builder()
                    .user(user)
                    .type(NotificationType.NOTICE)
                    .content(content)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(noti);
        });
    }

}
