package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.ResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name="Auction_Result")
public class AuctionResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id", nullable = false)
    private Long resultId;

    @OneToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionProductsEntity auction;

    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = true)
    private UserEntity winner;

//    @OneToOne
//    @JoinColumn(name = "order_id", nullable = true)
//    private OrderEntity order;
//
//    @OneToOne
//    @JoinColumn(name = "history_id", nullable = false)
//    private  AuctionHistoryEntity history;

    @Column(name = "result_status", nullable = false)
    private ResultStatus resultStatus;

    @Column(name = "final_price", nullable = false)
    private Integer finalPrice;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;
}
