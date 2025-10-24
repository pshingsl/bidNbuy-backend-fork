package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryResponse {
    private Long inquiriesId;
    private String title;
    private String content;
    private InquiryEnums.InquiryStatus status;
    private InquiryEnums.InquiryType type;
    private LocalDateTime createdAt;
    // 관리자 답변 추가
    private String requestTitle;
    private String requestContent;

    public static InquiryResponse fromEntity(InquiriesEntity inquiriesEntity) {
        return InquiryResponse.builder()
                .inquiriesId(inquiriesEntity.getInquiriesId())
                .title(inquiriesEntity.getTitle())
                .content(inquiriesEntity.getContent())
                .status(inquiriesEntity.getStatus())
                .type(inquiriesEntity.getType())
                .createdAt(inquiriesEntity.getCreatedAt())
                .requestTitle(inquiriesEntity.getRequestTitle())     // 관리자 답변 제목
                .requestContent(inquiriesEntity.getRequestContent()) // 관리자 답변 내용
                .build();
    }
}
