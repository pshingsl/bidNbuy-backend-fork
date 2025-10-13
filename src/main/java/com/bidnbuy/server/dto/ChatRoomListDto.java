package com.bidnbuy.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatRoomListDto {
    private String chatroomId;
    private String auctionId;

    private String counterpartId;
    private String counterpartNickname;
    private String counterpartProfileImageUrl;

    private String auctionTitle;
    private String auctionImageUrl;

    private LocalDateTime lastMessageTime;
    private String lastMessagePreview;
    private int unreadCount;

    public ChatRoomListDto(
            Long chatroomId, Long auctionId,
            Long counterpartId, String counterpartNickname, String counterpartProfileImageUrl,
            String auctionTitle, String auctionImageUrl,
            LocalDateTime lastMessageTime, String lastMessagePreview, int unreadCount){

        this.chatroomId = String.valueOf(chatroomId);
        this.auctionId = String.valueOf(auctionId);
        this.counterpartId = String.valueOf(counterpartId);

        this.counterpartNickname = counterpartNickname;
        this.counterpartProfileImageUrl = counterpartProfileImageUrl;
        this.auctionTitle = auctionTitle;
        this.auctionImageUrl = auctionImageUrl;
        this.lastMessageTime = lastMessageTime;
        this.lastMessagePreview = lastMessagePreview;
        this.unreadCount = unreadCount;
    }
}
