package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 리포지토리
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
}