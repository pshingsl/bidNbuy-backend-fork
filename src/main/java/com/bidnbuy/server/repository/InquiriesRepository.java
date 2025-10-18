package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiriesRepository extends JpaRepository<InquiriesEntity, Long> {

    // 일반 조회(전체, 상태필터링) (문의)
    List<InquiriesEntity> findByUserUserIdAndType(Long userId, InquiryEnums.InquiryType type);

    // 상세 조회 (문의)
    Optional<InquiriesEntity> findByInquiriesIdAndUserUserIdAndType(Long inquiryId, Long userId, InquiryEnums.InquiryType type);

    // 일반 조회(신고)
    List<InquiriesEntity> findByUser_UserIdAndType(Long userId, InquiryEnums.InquiryType type);

    // 상세 조회(신고)
    Optional<InquiriesEntity> findByInquiriesIdAndType(Long inquiriesId, InquiryEnums.InquiryType type);
}
