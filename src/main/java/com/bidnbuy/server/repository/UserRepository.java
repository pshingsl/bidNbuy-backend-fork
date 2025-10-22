package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("select user from UserEntity user where user.email = :email and user.deletedAt is null")
    Optional<UserEntity> findByEmail(String email); //로그인, 정보 조회
    Boolean existsByEmail(String email); //아이디(이메일) 중복확인
    @Query("select user from UserEntity user where user.email = :email")
    Optional<UserEntity> findByEmailWithDeleted(String email);

    // 관리자용
    // 모든 사용자 조회 (강퇴 포함)
    @Query("SELECT u FROM UserEntity u")
    Page<UserEntity> findAllIncludingDeleted(Pageable pageable);

    // 이메일 검색 (강퇴 포함)
    @Query("SELECT u FROM UserEntity u WHERE u.email LIKE %:email%")
    Page<UserEntity> findByEmailContainingIgnoreCaseIncludingDeleted(String email, Pageable pageable);

    // 정지된 사용자 조회 (스케줄러용)
    List<UserEntity> findByIsSuspendedTrue();
    
    // 페널티 점수 범위로 사용자 조회
    List<UserEntity> findByPenaltyPointsGreaterThan(int points);
    
    // 정지 해제된 사용자 조회
    @Query("SELECT u FROM UserEntity u WHERE u.isSuspended = true AND u.suspendedUntil < CURRENT_TIMESTAMP")
    List<UserEntity> findExpiredSuspensions();
}
