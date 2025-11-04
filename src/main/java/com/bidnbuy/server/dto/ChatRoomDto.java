package com.bidnbuy.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private long chatroomId;
    private long buyerId;
    private long sellerId;
    private long auctionId;
    private LocalDateTime createdAt;

    private String lastMessagePreview;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
}
