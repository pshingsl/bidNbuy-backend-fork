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

    public static InquiryResponse fromEntity(InquiriesEntity inquiriesEntity) {
        return InquiryResponse.builder()
                .inquiriesId(inquiriesEntity.getInquiriesId())
                .title(inquiriesEntity.getTitle())
                .content(inquiriesEntity.getContent())
                .status(inquiriesEntity.getStatus())
                .type(inquiriesEntity.getType())
                .createdAt(inquiriesEntity.getCreatedAt())
                .build();
    }
}
