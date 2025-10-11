package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class UserImageDto {
    private String imageUrl;
}
