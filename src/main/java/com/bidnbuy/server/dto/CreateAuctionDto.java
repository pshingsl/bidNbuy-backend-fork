package com.bidnbuy.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateAuctionDto {
    @NotNull(message = "카테고리는 필수 선택 항목입니다.")
    private Integer categoryId;

    @NotNull(message = "상품 이미지는 필수입니다.")
    @Size(min = 1, max = 10, message = "이미지는 1장 이상 10장 이하로 등록해야 합니다.")
    private List<ImageDto> images;

    @NotBlank(message = "상품명은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "상품 설명은 필수 입력 항목입니다.")
    private String description;

    @NotNull(message = "시작 가격은 필수 입력 항목입니다.")
    @Min(value = 1, message = "시작 가격은 1원 이상이어야 합니다.")
    private Integer startPrice;

    private Integer minBidPrice;

    @NotNull(message = "경매 시작 일시는 필수 입력 항목입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "경매 마감 일시는 필수 입력 항목입니다.")
    private LocalDateTime endTime;
}