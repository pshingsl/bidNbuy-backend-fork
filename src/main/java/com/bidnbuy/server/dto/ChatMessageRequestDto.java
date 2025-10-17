package com.bidnbuy.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestDto {
    private Long chatroomId;
    private String message;
    private String type;

}
