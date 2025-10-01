package com.bidnbuy.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// 카테고리 테이블
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CategoryEntity parent;

    // 하위 카테고리 중분류
    @OneToMany(mappedBy = "parent")
    private List<CategoryEntity> children = new ArrayList<>();

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @OneToMany(mappedBy = "category")
    private List<AuctionProductsEntity> auctionProducts = new ArrayList<>();
}