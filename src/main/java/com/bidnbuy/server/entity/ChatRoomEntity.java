package com.bidnbuy.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="Chatroom")
public class ChatRoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chatroom_id", nullable = false)
    private long chatroomId; //pk auto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="buyer_id", nullable = false)
    private UserEntity buyerId; //fk

    @ManyToOne
    @JoinColumn(name="seller_id", nullable = false)
    private UserEntity sellerId; //fk

    @ManyToOne
    @JoinColumn(name="auction_id", nullable = false)
    private AuctionProductsEntity auctionId; //fk

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
