package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.InquiryEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInquiryStatusRequestDto {
    
    @NotNull(message = "상태는 필수입니다")
    private InquiryEnums.InquiryStatus status;
}