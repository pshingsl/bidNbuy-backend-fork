package com.bidnbuy.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BidRequestDto {
    @NotNull(message = "입찰 금액은 필수 입력 항목입니다.") // 값이 null이 아닌지 검증
    @Min(value = 100, message = "입찰 금액은 최소 100원 이상이어야 합니다.") // 0원 또는 음수 입찰 방지
    private Integer bidPrice;
}
