package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.UserFcmTokenEntity;
import com.bidnbuy.server.enums.NotificationType;
import com.bidnbuy.server.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserFcmTokenRepository userTokenRepository;

    public void sendNotification(Long userId, String title, String body, String type, Long notiId, LocalDateTime createdAt) {
        List<UserFcmTokenEntity> tokens = userTokenRepository.findByUser_UserId(userId);

        if (tokens.isEmpty()) {
            throw new RuntimeException("No FCM token for user");
        }

        //setNotification => OS측에서 백그라운드 알림, 나머지 putData => 모달, 포그라운드 알림에서 사용가능하게 변경
        for (UserFcmTokenEntity t : tokens) {
            Message message = Message.builder()
                    .setToken(t.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("type", type)
                    .putData("content", body)
                    .putData("notificationId", String.valueOf(notiId))
                    .putData("createdAt", createdAt.toString())
                    .build();

            try {
                FirebaseMessaging.getInstance().send(message);
            } catch (Exception e) {
                throw new RuntimeException("FCM 전송 실패", e);
            }
        }
    }

}
