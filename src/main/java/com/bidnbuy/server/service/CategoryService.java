package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.CategoryDto;
import com.bidnbuy.server.entity.CategoryEntity;
import com.bidnbuy.server.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// 카테고리 서비스
@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    // 대분류
    @Transactional(readOnly = true)
    public List<CategoryDto> findAllCategoryType() {
        List<CategoryEntity> topLevelCategory = categoryRepository.findByParentIsNull();
        return topLevelCategory.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }

    // 중분류
    @Transactional(readOnly = true)
    public List<CategoryDto> findChildrenByParentId(Long parentId){

        // 유효성 검사
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 부모 카테고리 ID입니다: " + parentId));

        // 하위 카테고리 조회(소분류 목록)
        List<CategoryEntity> children = categoryRepository.findByParent_CategoryId(parentId);
        return children.stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());
    }
}
