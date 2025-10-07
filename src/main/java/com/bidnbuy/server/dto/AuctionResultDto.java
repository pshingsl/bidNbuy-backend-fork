package com.bidnbuy.server.dto;


import com.bidnbuy.server.enums.ResultStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AuctionResultDto {

    private Long auctionId;
    private String title;
    private String itemImageUrl;

    private String sellerNickName;
    private Integer finalPrice;
    private LocalDateTime endTime; // 월/일/시간까지 출력하기 위해 사용

    private String status; // 클라이언트 표시를 위해서 사용
}
