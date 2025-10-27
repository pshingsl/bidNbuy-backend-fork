package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "11", required = true)
    private Long categoryId;

    @NotBlank(message = "상품명은 필수 입력 항목입니다.")
    @Schema(example = "경매 등록 제목", required = true)
    private String title;

    @NotBlank(message = "상품 설명은 필수 입력 항목입니다.")
    @Schema(example = "경매 상품 소개글", required = true)
    private String description;

    @NotNull(message = "시작 가격은 필수 입력 항목입니다.")
    @Schema(example = "10000", required = true)
    @Min(value = 1, message = "시작 가격은 1원 이상이어야 합니다.")
    private Integer startPrice;

    @NotNull(message = "경매 입찰금액 필수 입력 항목입니다.")
    @Schema(description = "입찰금액", example = "1000", required = true)
    @Min(value = 1, message = "입찰 가격은 시작가보다 클 수 없습니다.")
    private Integer minBidPrice;

    @NotNull(message = "경매 시작 일시는 필수 입력 항목입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "경매 마감 일시는 필수 입력 항목입니다.")
    private LocalDateTime endTime;

    @NotNull(message = "이미지는 필수 입력 항목입니다.")
    @Size(min = 1, max = 10, message = "이미지 첨부는 1~10장까지 첨부 가능합니다.")
    private List<MultipartFile> images;

    public CreateAuctionDto(final AuctionProductsEntity entity) {
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.startPrice = entity.getStartPrice();
        this.minBidPrice = entity.getMinBidPrice();
        this.startTime = entity.getStartTime();
        this.endTime = entity.getEndTime();
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

