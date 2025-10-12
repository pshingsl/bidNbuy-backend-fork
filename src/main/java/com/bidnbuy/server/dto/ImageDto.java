package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ImageEntity;
import lombok.*;

// 이미지 디티오
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {
    private String imageUrl;
    private String imageType;

    public ImageDto(ImageEntity entity) {
        this.imageUrl = entity.getImageUrl();
        this.imageType = entity.getImageType();
    }

    public  ImageEntity toEntity(final AuctionProductsEntity auctionProducts){
        return ImageEntity.builder()
                .auctionProduct(auctionProducts)
                .imageUrl(this.imageUrl)
                .imageType(this.imageType)
                .build();
    }
}