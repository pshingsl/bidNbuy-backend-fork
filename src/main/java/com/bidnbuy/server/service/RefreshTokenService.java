package com.bidnbuy.server.service;

import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.entity.RefreshTokenEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveOrUpdate(UserEntity user, String tokenValue, Instant expiryDate){
        Optional<RefreshTokenEntity> existingToken = refreshTokenRepository.findByUser(user);
        if(existingToken.isPresent()){
            RefreshTokenEntity token = existingToken.get();
            token.updateToken(tokenValue, expiryDate);
            refreshTokenRepository.saveAndFlush(token);
        }else{
            RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                    .user(user)
                    .tokenValue(tokenValue)
                    .expiryDate(expiryDate)
                    .build();
            refreshTokenRepository.save(newToken);
        }
    }

    @Transactional(readOnly = true)
    public Optional<RefreshTokenEntity> findByTokenValue(String tokenValue){
        return refreshTokenRepository.findByTokenValue(tokenValue);
    }

    @Transactional
    public void deleteByTokenValue(String tokenValue){
        refreshTokenRepository.findByTokenValue(tokenValue).ifPresent(refreshTokenRepository::delete);
    }

    // 관리자용 refresh token 저장/업데이트
    @Transactional
    public void saveOrUpdateForAdmin(AdminEntity admin, String tokenValue, Instant expiryDate){
        Optional<RefreshTokenEntity> existingToken = refreshTokenRepository.findByAdmin(admin);
        if(existingToken.isPresent()){
            RefreshTokenEntity token = existingToken.get();
            token.updateToken(tokenValue, expiryDate);
            refreshTokenRepository.saveAndFlush(token);
        }else{
            RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                    .admin(admin)
                    .tokenValue(tokenValue)
                    .expiryDate(expiryDate)
                    .build();
            refreshTokenRepository.save(newToken);
        }
    }
}