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
@Table(name="ChatMessage")
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chatmessage_id", nullable = false)
    private long chatmessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chatroom_id", nullable = false)
    private ChatRoomEntity chatroomId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name="buyer_id", nullable = false)
//    private UserEntity buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sender_id", nullable = false)
    private UserEntity senderId;

    @Column(name="image_url")
    private String imageUrl;

    @Column(name="message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name="message_type", nullable = false)
    private MessageType messageType = MessageType.CHAT;

    @CreationTimestamp
    @Column(name="create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name="is_read", nullable = false)
    private boolean isRead = false;

    public enum MessageType{
        CHAT,
        REQUEST,
        SYSTEM
    }
}

