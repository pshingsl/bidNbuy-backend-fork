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
    private long chatmessageId;
    private long chatroomId;
    private long senderId;
    private String message;
//    private long buyerId;
//    private long sellerId;
    private String imageUrl;
    private ChatMessageEntity.MessageType messageType;
    private LocalDateTime createdAt;
    private boolean isRead;
}
