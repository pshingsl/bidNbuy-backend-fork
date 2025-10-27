package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.CategoryEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;


@Data
@Builder
public class CategoryDto {
    private Long categoryId;
    private String categoryName;
    private Long parentId;
    private List<CategoryDto> children;

    public static CategoryDto from(CategoryEntity entity) {
        List<CategoryDto> dtos = entity.getChildren().stream()
                .map(CategoryDto::from)
                .collect(Collectors.toList());

        return  CategoryDto.builder()
                .categoryId(entity.getCategoryId())
                .categoryName(entity.getCategoryName())
                .parentId(entity.getParent() != null ? entity.getParent().getCategoryId() : null)
                .children(dtos)
                .build();
    }
}
