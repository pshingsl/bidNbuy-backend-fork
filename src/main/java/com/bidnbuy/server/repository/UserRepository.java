package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    UserEntity findByEmailAndDeletedAtIsNull(String email);

    // 관리자용
    // 모든 사용자 조회 (삭제 포함)
    @Query(value = "SELECT * FROM user ORDER BY created_at DESC", nativeQuery = true)
    java.util.List<UserEntity> findAllIncludingDeleted();

    // 이메일 검색 (삭제 포함)
    @Query(value = "SELECT * FROM user WHERE email LIKE CONCAT('%', :email, '%') ORDER BY created_at DESC", nativeQuery = true)
    java.util.List<UserEntity> findByEmailContainingIgnoreCaseIncludingDeleted(String email);

    // 삭제 유저 포함 상세
    @Query(value = "SELECT * FROM user WHERE user_id = :userId", nativeQuery = true)
    Optional<UserEntity> findByIdIncludingDeleted(Long userId);

    // 정지된 사용자 조회 (스케줄러용)
    List<UserEntity> findByIsSuspendedTrue();
    
    // 페널티 점수 범위로 사용자 조회
    List<UserEntity> findByPenaltyPointsGreaterThan(int points);
    
    // 정지 해제된 사용자 조회
    @Query("SELECT u FROM UserEntity u WHERE u.isSuspended = true AND u.suspendedUntil < CURRENT_TIMESTAMP")
    List<UserEntity> findExpiredSuspensions();

    // 락 메서드 - 페널티 부과 관련 동시성 제어 위해
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.userId = :userId")
    Optional<UserEntity> findByIdWithLock(Long userId);
}
