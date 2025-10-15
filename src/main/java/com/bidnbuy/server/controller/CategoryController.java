package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CategoryDto;
import com.bidnbuy.server.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 카테고리 컨트롤러
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/top")
    public ResponseEntity<?> topLevelCategory() {
        try {
            List<CategoryDto> top = categoryService.findAllCategoryType();

            return ResponseEntity.ok(top);
        }catch (Exception e) {
            log.error("최상위 카테고리 목록 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<CategoryDto>> getChildrenCategories(@PathVariable Integer parentId) {
        try {
            // Service의 findChildrenByParentId() 호출 (findByParent_CategoryId 기반)
            List<CategoryDto> children = categoryService.findChildrenByParentId(parentId);

            return ResponseEntity.ok(children);

        } catch (IllegalArgumentException e) {
            // 유효성 검증 실패 시 400 Bad Request 응답
            log.warn("유효하지 않은 부모 ID 요청: {}", parentId);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("하위 카테고리 조회 중 오류 발생. Parent ID: {}", parentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategoryDetails(@PathVariable Integer categoryId) {
        try {
            CategoryDto category = categoryService.findById(categoryId);

            if (category == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(category);
        } catch (Exception e) {
            log.error("단일 카테고리 조회 중 오류 발생. ID: {}", categoryId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
