package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAuctionDto {
    @NotNull(message = "카테고리는 필수 선택 항목입니다.")
    private Integer categoryId;


    @NotBlank(message = "상품명은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "상품 설명은 필수 입력 항목입니다.")
    private String description;

    @NotNull(message = "시작 가격은 필수 입력 항목입니다.")
    @Min(value = 1, message = "시작 가격은 1원 이상이어야 합니다.")
    private Integer startPrice;

    @NotNull(message = "경매 입찰금액 필수 입력 항목입니다.")
    private Integer minBidPrice;

    @NotNull(message = "경매 시작 일시는 필수 입력 항목입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "경매 마감 일시는 필수 입력 항목입니다.")
    private LocalDateTime endTime;

    private List<MultipartFile> images;

    public CreateAuctionDto(final AuctionProductsEntity entity) {
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startPrice = entity.getStartPrice();
        this.minBidPrice = entity.getMinBidPrice();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
//        this.images = entity.getImages().stream()
//                .map(ImageDto::new)
//                .collect(Collectors.toList());
        this.categoryId = entity.getCategory().getCategoryId();
    }

    public static AuctionProductsEntity toEntity(final CreateAuctionDto dto) {
        return AuctionProductsEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startPrice(dto.getStartPrice())
                .minBidPrice(dto.getMinBidPrice())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }
}

