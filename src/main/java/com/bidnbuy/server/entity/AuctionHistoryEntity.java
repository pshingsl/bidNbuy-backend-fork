package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="Auction_History")
public class AuctionHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionProductsEntity auctionProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "previos_status")
    private AuctionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private AuctionStatus newStatus;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

}
