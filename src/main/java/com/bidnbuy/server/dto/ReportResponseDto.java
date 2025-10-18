package com.bidnbuy.server.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponseDto {
    private Long reportId;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private String requestTitle;   // 관리자 답변 제목
    private String requestContent; // 관리자 답변 내용
}
