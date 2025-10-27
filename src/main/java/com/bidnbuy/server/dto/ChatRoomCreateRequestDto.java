package com.bidnbuy.server.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomCreateRequestDto {
    @NotNull
    private Long buyerId;

    @NotNull
    private Long auctionId;
}
