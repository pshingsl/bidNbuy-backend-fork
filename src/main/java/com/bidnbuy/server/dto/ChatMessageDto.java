package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.ChatMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String chatmessageId;
    private String chatroomId;
    private String senderId;
    private String message;
//    private long buyerId;
//    private long sellerId;
    private String imageUrl;
    private ChatMessageEntity.MessageType messageType;
    private LocalDateTime createdAt;
    private boolean isRead;
}
