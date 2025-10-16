package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.paymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment")
@Getter
@Setter
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // 토스에 넘긴 '머천트 주문번호' (토스 API의 orderId)
    @Column(nullable = false, length = 225, unique = true)
    private String merchantOrderId;

    @Column(nullable = true, length = 225)
    private String tossPaymentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 50)
    private paymentStatus.PaymentMethod tossPaymentMethod; // enum

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private paymentStatus.PaymentStatus tossPaymentStatus; // enum SUCCESS, FAIL, CANCEL, REFUND

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    // ✅ 결제 취소 내역
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentCancelEntity> cancels = new ArrayList<>();
}
