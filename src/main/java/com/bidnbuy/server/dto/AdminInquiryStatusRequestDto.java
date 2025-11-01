package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.InquiryEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 문의 답변 상태 변경")
public class AdminInquiryStatusRequestDto {
    
    @NotNull(message = "상태는 필수입니다")
    @Schema(description = "변경하려는 문의 답변 상태", example = "WAITING", required = true)
    private InquiryEnums.InquiryStatus status;
}