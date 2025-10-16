package com.bidnbuy.server.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Settlement")
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Integer payoutAmount;

    @Column(length = 255, nullable = false)
    private String payoutStatus; // WAITING, DONE, HOLD

    private LocalDateTime payoutAt;
}
