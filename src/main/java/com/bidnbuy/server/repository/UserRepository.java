package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("select user from UserEntity user where user.email = :email and user.deletedAt is null")
    Optional<UserEntity> findByEmail(String email); //로그인, 정보 조회
    Boolean existsByEmail(String email); //아이디(이메일) 중복확인
    @Query("select user from UserEntity user where user.email = :email")
    Optional<UserEntity> findByEmailWithDeleted(String email);
}
