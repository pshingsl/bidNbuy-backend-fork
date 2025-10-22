package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuspensionScheduler {
    
    private final UserRepository userRepository;

    // 매일 자정 정지 해제 체크
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkSuspensions() {
        log.info("정지 해제 스케줄러 실행");
        
        List<UserEntity> suspendedUsers = userRepository.findByIsSuspendedTrue();
        
        int releasedCount = 0;
        for (UserEntity user : suspendedUsers) {
            if (user.getSuspendedUntil() != null && 
                user.getSuspendedUntil().isBefore(LocalDateTime.now())) {
                
                user.setSuspended(false);
                user.setSuspendedUntil(null);
                userRepository.save(user);
                releasedCount++;
                
                log.info("정지 해제: userId={}, nickname={}", user.getUserId(), user.getNickname());
            }
        }
        
        log.info("정지 해제 스케줄러 완료: {}명 해제", releasedCount);
    }
}