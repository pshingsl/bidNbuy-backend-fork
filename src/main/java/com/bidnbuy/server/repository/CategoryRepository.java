package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 카테고리 리포지토리
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // 대분류 카테고리 조회
    List<CategoryEntity> findByParentIsNull();

    // 대분류 속한 중분류 카테고리 조회
    List<CategoryEntity> findByParent_CategoryId(Long parentId);
}