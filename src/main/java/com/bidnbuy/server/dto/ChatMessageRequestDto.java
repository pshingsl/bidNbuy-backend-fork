package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.ChatMessageEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestDto {
    private Long chatroomId;
    private String message;
    private ChatMessageEntity.MessageType messageType;;

}
