package com.bidnbuy.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 문의 답변")
public class AdminInquiryReplyRequestDto {
    
    @NotBlank(message = "답변 제목은 필수입니다")
    @Size(max = 50, message = "답변 제목은 50자 이하여야 합니다")
    @Schema(description = "답변 제목", example = "환불 불가", required = true, maxLength = 50)
    private String title;
    
    @NotBlank(message = "답변 내용은 필수입니다")
    @Size(max = 1000, message = "답변 내용은 1000자 이하여야 합니다")
    @Schema(description = "답변 내용", example = "판매자랑 얘기하세요.", required = true, maxLength = 1000)
    private String content;
}