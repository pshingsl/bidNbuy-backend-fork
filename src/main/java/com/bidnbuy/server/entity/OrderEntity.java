package com.bidnbuy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
@Getter
@Setter
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // ✅ 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserEntity seller;

    // ✅ 구매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserEntity buyer;

    // ✅ 경매 결과
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    private AuctionResultEntity result;
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private int rating = 0;

    @Column(length = 50, nullable = false)
    private String orderStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ✅ 연관관계
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private SettlementEntity settlement;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private PaymentEntity payment;
}
