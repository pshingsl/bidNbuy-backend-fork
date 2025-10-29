package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "settlement")
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Integer payoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private SettlementStatus payoutStatus; // WAITING, DONE, HOLD

    private LocalDateTime payoutAt;
}
