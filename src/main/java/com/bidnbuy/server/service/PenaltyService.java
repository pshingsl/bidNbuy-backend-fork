package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.PenaltyEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.NotificationType;
import com.bidnbuy.server.enums.PenaltyType;
import com.bidnbuy.server.repository.PenaltyRepository;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final UserRepository userRepository;
    private final UserNotificationService userNotificationService;

    // 페널티 부과
    public void applyPenalty(Long userId, PenaltyType type) {
        log.info("페널티 부과 시작: userId={}, type={}", userId, type);

        // 동시성 제어 위해 비관적 락 사용
        UserEntity user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 페널티 기록
        PenaltyEntity penalty = PenaltyEntity.builder()
                .user(user)
                .type(type)
                .points(type.getPoints())
                .build();
        penaltyRepository.save(penalty);

        // 누적 업데이트
        int newTotalPoints = user.getPenaltyPoints() + type.getPoints();
        user.setPenaltyPoints(newTotalPoints);

        log.info("누적 페널티 점수 업데이트: {} -> {}", user.getPenaltyPoints() - type.getPoints(), newTotalPoints);

        // 제재 로직 실행
        checkAndApplySanctions(user, newTotalPoints);

        // 페널티 알림 전송
        String penaltyMessage = "페널티 부과 안내: 서비스의 정책사항을 위반한 행위로 "
                + type.getPoints() + "점이 부과되었습니다.";


        try {
            userNotificationService.createNotification(userId, NotificationType.WARN, penaltyMessage);
            log.info("✅ 페널티 알림 전송 완료: userId={}, message={}", userId, penaltyMessage);
        } catch (Exception e) {
            log.warn("⚠️ 페널티 알림 전송 실패: {}", e.getMessage());
        }

        userRepository.save(user);
        log.info("페널티 부과 완료: userId={}, totalPoints={}", userId, newTotalPoints);
    }

    // 제재 체크 및 적용
    private void checkAndApplySanctions(UserEntity user, int totalPoints) {
        // 50점 이상 강퇴 (최초 1회)
        if (totalPoints >= 50 && user.getBanCount() == 0) {
            user.setBanCount(1);
            user.setDeletedAt(LocalDateTime.now()); // Soft Delete
            log.warn("사용자 강퇴: userId={}, totalPoints={}", user.getUserId(), totalPoints);
        }
        // 30점 이상 6개월 정지 (최초 1회)
        else if (totalPoints >= 30 && user.getSuspensionCount() == 0) {
            user.setSuspensionCount(1);
            // 개발 -> 배포 시 변경 (완료)
            user.setSuspendedUntil(LocalDateTime.now().plusMonths(6)); // 6개월 정지
            // user.setSuspendedUntil(LocalDateTime.now().plusMinutes(15)); // 개발 테스트용 15분 정지
            user.setSuspended(true);
            log.warn("사용자 정지: userId={}, totalPoints={}, 해제일={}", user.getUserId(), totalPoints, user.getSuspendedUntil());
        }
    }

    // 정지 해제 체크 (로그인 시)
    public void checkSuspensionExpiry(UserEntity user) {
        if (user.isSuspended() && user.getSuspendedUntil() != null) {
            if (user.getSuspendedUntil().isBefore(LocalDateTime.now())) {
                user.setSuspended(false);
                user.setSuspendedUntil(null);
                userRepository.save(user);
                log.info("정지 해제: userId={}", user.getUserId());
            }
        }
    }
}