package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.SellingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// 경매 테이블
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="AuctionProducts")
public class AuctionProducts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long auctionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Long wishlistId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "start_price", nullable = false)
    private int start_price;

    @Column(name = "min_bid_price", nullable = false)
    private int min_bid_price;

    @Enumerated(EnumType.STRING)
    @Column(name = "selling_status", nullable = false)
    private SellingStatus selling_status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start_time;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime end_time;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime create_time;

    @Column(name = "delete_at", nullable = false)
    private LocalDateTime delete_at;
}
