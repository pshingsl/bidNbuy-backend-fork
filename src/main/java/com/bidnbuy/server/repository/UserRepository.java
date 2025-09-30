package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email); //로그인, 정보 조회
    Boolean existsByEmail(String email); //아이디(이메일) 중복확인
}
