package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Data;

// 이미지 디티오
@Data
@Builder
public class ImageDto {
    private String imageUrl;
    private String imageType;
}