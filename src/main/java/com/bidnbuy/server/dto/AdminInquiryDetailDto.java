package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.InquiryEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInquiryDetailDto {
    private Long inquiriesId;
    private String title;
    private String content;
    private InquiryEnums.InquiryType type;
    private InquiryEnums.InquiryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    
    // 작성자
    private Long userId;
    private String userEmail;
    private String userNickname;
    
    // 관리자
    private Long adminId;
    private String adminEmail;
    
    // 답변
    private String requestTitle;
    private String requestContent;
}