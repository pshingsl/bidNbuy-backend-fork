package com.bidnbuy.server.dto;

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
    private String chatroomId;
    private String buyerId;
    private String sellerId;
    private String auctionId;
    private LocalDateTime createdAt;
}
