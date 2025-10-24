// service/FcmService.java
package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.UserFcmTokenEntity;
import com.bidnbuy.server.repository.UserFcmTokenRepository;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserFcmTokenService {

    private final UserFcmTokenRepository tokenRepository;
    private final UserRepository userRepository;


    public void registerToken(Long userId, String token) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이미 있으면 무시 (중복 방지)
        tokenRepository.findByUserAndToken(user, token)
                .orElseGet(() -> {
                    UserFcmTokenEntity entity = UserFcmTokenEntity.builder()
                            .user(user)
                            .token(token)
                            .build();
                    return tokenRepository.save(entity);
                });
    }
}