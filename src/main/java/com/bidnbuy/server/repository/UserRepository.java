package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("select user from UserEntity user where user.email = :email and user.deletedAt is null")
    Optional<UserEntity> findByEmail(String email); //로그인, 정보 조회
    Boolean existsByEmail(String email); //아이디(이메일) 중복확인

    @Query(value = "SELECT * FROM user WHERE email = :email ORDER BY deleted_at DESC, user_id DESC LIMIT 1", nativeQuery = true)
    Optional<UserEntity> findByEmailWithDeleted(String email);

    Optional<UserEntity> findByNickname(String nickname);

    // 관리자용
    // 모든 사용자 조회 (강퇴 포함)
    @Query(value = "SELECT * FROM user ORDER BY created_at DESC", nativeQuery = true)
    java.util.List<UserEntity> findAllIncludingDeleted();

    // 이메일 검색 (강퇴 포함)
    @Query(value = "SELECT * FROM user WHERE email LIKE CONCAT('%', :email, '%') ORDER BY created_at DESC", nativeQuery = true)
    java.util.List<UserEntity> findByEmailContainingIgnoreCaseIncludingDeleted(String email);

    // 정지된 사용자 조회 (스케줄러용)
    List<UserEntity> findByIsSuspendedTrue();
    
    // 페널티 점수 범위로 사용자 조회
    List<UserEntity> findByPenaltyPointsGreaterThan(int points);
    
    // 정지 해제된 사용자 조회
    @Query("SELECT u FROM UserEntity u WHERE u.isSuspended = true AND u.suspendedUntil < CURRENT_TIMESTAMP")
    List<UserEntity> findExpiredSuspensions();
}
