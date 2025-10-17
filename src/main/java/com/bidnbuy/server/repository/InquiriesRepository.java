package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.Inquiries;
import com.bidnbuy.server.enums.InquiryEnums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiriesRepository extends JpaRepository<Inquiries, Long> {

    // 일반 조회(전체, 상태필터링)
    List<Inquiries> findByUserUserId(Long userId);
    List<Inquiries> findByUserUserIdAndStatus(Long userId, InquiryEnums.InquiryStatus status);

    // 상세 조회
    Optional<Inquiries> findByInquiriesIdAndUserUserId(Long inquiryId, Long userId);
}
