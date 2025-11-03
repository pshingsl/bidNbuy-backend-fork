package com.bidnbuy.server.dto;

import com.bidnbuy.server.enums.InquiryEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자용 문의 상세")
public class AdminInquiryDetailDto {
    @Schema(description = "문의 ID", example = "1")
    private Long inquiriesId;
    
    @Schema(description = "문의 제목", example = "환불 문의")
    private String title;
    
    @Schema(description = "문의 내용", example = "환불받고 싶어요.")
    private String content;
    
    @Schema(description = "문의 타입", example = "GENERAL")
    private InquiryEnums.InquiryType type;
    
    @Schema(description = "문의 상태", example = "COMPLETE")
    private InquiryEnums.InquiryStatus status;
    
    @Schema(description = "문의 작성일", example = "2025-10-24T16:14:31.991996")
    private LocalDateTime createdAt;
    
    @Schema(description = "문의 수정일", example = "2025-10-24T16:16:41.573054")
    private LocalDateTime updateAt;
    
    @Schema(description = "문의 유저 ID", example = "1")
    private Long userId;
    
    @Schema(description = "유저 이메일", example = "user@example.com")
    private String userEmail;
    
    @Schema(description = "유저 닉네임 (탈퇴 회원인 경우 '탈퇴회원')", example = "사용자1")
    private String userNickname;
    
    @Schema(description = "답변 관리자 ID", example = "1")
    private Long adminId;
    
    @Schema(description = "답변 제목", example = "환불 불가")
    private String requestTitle;
    
    @Schema(description = "답변 내용", example = "판매자랑 얘기하세요.")
    private String requestContent;
}