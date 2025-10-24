package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.UserFcmTokenEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmTokenEntity, Long> {
    Optional<UserFcmTokenEntity> findByUserAndToken(UserEntity user, String token);

    // 유저가 가진 모든 토큰 조회
    List<UserFcmTokenEntity> findByUser_UserId(Long userId);

    // 특정 토큰으로 검색 (중복 방지 등)
    Optional<UserFcmTokenEntity> findByUser_UserIdAndToken(Long userId, String token);

    // 토큰 삭제 (로그아웃, 권한 철회 등)
    Optional<UserFcmTokenEntity> findByToken(String token);

    // 유저별 모든 토큰 삭제 (회원 탈퇴 시)
    @Modifying
    @Transactional
    @Query("DELETE FROM UserFcmTokenEntity t WHERE t.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

}
