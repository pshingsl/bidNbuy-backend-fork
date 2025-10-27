package com.bidnbuy.server.dto;

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
public class AdminInquiryReplyRequestDto {
    
    @NotBlank(message = "답변 제목은 필수입니다")
    @Size(max = 50, message = "답변 제목은 50자 이하여야 합니다")
    private String title;
    
    @NotBlank(message = "답변 내용은 필수입니다")
    @Size(max = 1000, message = "답변 내용은 1000자 이하여야 합니다")
    private String content;
}