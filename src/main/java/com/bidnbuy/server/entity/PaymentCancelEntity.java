package com.bidnbuy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_cancel")
public class PaymentCancelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cancelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    private String cancelReason;
    private Integer cancelAmount;

    // Toss 트랜잭션 키 (취소 고유 ID)
    @Column(nullable = false, length = 100)
    private String transactionKey;

    // Toss 취소 상태 (예: DONE)
    @Column(nullable = false, length = 50)
    private String cancelStatus;

    // 취소 영수증 키
    @Column(length = 255)
    private String receiptKey;

    // Toss 응답의 canceledAt
    @Column(nullable = false)
    private LocalDateTime canceledAt;

    // 로그 생성 시간 (DB 관리용)
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
