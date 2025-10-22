package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.ChatMessageEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestDto {
    private Long chatroomId;
    private String message;
    @Builder.Default
    private ChatMessageEntity.MessageType messageType = ChatMessageEntity.MessageType.CHAT;

}
