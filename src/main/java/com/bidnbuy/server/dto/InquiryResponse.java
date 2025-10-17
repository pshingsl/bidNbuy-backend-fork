package com.bidnbuy.server.dto;

import com.bidnbuy.server.entity.Inquiries;
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

    public static InquiryResponse fromEntity(Inquiries inquiries) {
        return InquiryResponse.builder()
                .inquiriesId(inquiries.getInquiriesId())
                .title(inquiries.getTitle())
                .content(inquiries.getContent())
                .status(inquiries.getStatus())
                .type(inquiries.getType())
                .createdAt(inquiries.getCreatedAt())
                .build();
    }
}
