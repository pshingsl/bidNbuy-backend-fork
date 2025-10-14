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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="seller_id", nullable = false)
    private UserEntity sellerId; //fk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="auction_id", nullable = false)
    private AuctionProductsEntity auctionId; //fk

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    //추가
    @Column(name = "last_message_preview", nullable = true)
    private String lastMessagePreview;

    @Column(name = "last_message_time", nullable = true)
    private LocalDateTime lastMessageTime;

    @Column(name = "unread_count", nullable = false)
    private int unreadCount = 0;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;
}
