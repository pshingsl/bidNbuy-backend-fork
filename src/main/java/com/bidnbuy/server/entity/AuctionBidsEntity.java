package com.bidnbuy.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
// 경매 시스템에서 자주바뀌는게 상품과 최고가이며 최고가를 찾기 위해서 @인덱스 사용
@Table(name = "Auction_Bids", indexes = { @Index(name = "auction_price",
        columnList = "auction_id, bid_price DESC")})
public class AuctionBidsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private AuctionProductsEntity auction;

    @Column(name = "bid_price")
    private Integer bidPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = true) // FK 컬럼 이름은 확인 필요
    private AuctionHistoryEntity history;

    @CurrentTimestamp
    @Column(name = "bid_time")
    private LocalDateTime bidTime;
}
