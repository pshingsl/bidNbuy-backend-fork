package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.CategoryDto;
import com.bidnbuy.server.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리 API", description = "카테고리 기능 제공")
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "카테고리 대분류 소분류", description = "카테고리 대분류 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryDto.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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

    @Operation(summary = "카테고리 소분류 API", description = "카테고리 소분류 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 부모 카테고리 ID"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<CategoryDto>> getChildrenCategories(@PathVariable Long parentId) {
        try {
            List<CategoryDto> children = categoryService.findChildrenByParentId(parentId);

            return ResponseEntity.ok(children);

        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 부모 ID 요청: {}", parentId);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("하위 카테고리 조회 중 오류 발생. Parent ID: {}", parentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "특정 ID의 단일 카테고리 상세 정보 API", description = "특정 ID의 단일 카테고리 상세 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 카테고리 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategoryDetails(@PathVariable Long categoryId) {
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
