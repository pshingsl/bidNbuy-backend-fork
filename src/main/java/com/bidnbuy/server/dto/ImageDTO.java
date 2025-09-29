package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.ImageEntity;
import lombok.Data;

@Data
public class ImageDTO {
    private String imageUrl;
    private String imageType;
}