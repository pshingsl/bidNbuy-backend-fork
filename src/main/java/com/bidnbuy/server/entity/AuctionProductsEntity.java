package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.SellingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// 경매상품 테이블
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "AuctionProducts")
public class AuctionProductsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long auctionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private WishLisEntity wishlist;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "start_price", nullable = false)
    private Integer startPrice;

    @Column(name = "current_price", nullable = false)
    private Integer currentPrice;

    @Column(name = "min_bid_price", nullable = false)
    private Integer minBidPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "selling_status", nullable = false)
    private SellingStatus sellingStatus;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "delete_at", nullable = false)
    private LocalDateTime deleteAt;
}
