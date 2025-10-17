package com.bidnbuy.server.enums;

public class InquiryEnums {
    public enum InquiryType {
        GENERAL, // 일반문의
        REPORT   // 신고
    }

    public enum InquiryStatus {
        WAITING,   // 답변대기
        COMPLETE   // 답변완료
    }
}
