package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.PenaltyEntity;
import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<PenaltyEntity, Long> {
    
    // 사용자별 페널티 히스토리 조회 (최신순)
    List<PenaltyEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    
    // 사용자별 활성 페널티만 조회 (최신순)
    List<PenaltyEntity> findByUserAndIsActiveTrueOrderByCreatedAtDesc(UserEntity user);
    
    // 사용자별 페널티 개수 조회
    long countByUser(UserEntity user);
}
