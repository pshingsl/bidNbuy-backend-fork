package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.ChatMessageEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String imageUrl; //s3사용
    private ChatMessageEntity.MessageType messageType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private boolean isRead;
}
