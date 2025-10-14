package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.entity.RefreshTokenEntity;
import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue);

    Optional<RefreshTokenEntity> findByUser (UserEntity user);
    
    Optional<RefreshTokenEntity> findByAdmin(AdminEntity admin);

}
