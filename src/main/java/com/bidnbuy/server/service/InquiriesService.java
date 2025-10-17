package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.CreateInquiryRequest;
import com.bidnbuy.server.dto.InquiryResponse;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.entity.Inquiries;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import com.bidnbuy.server.repository.AdminRepository;
import com.bidnbuy.server.repository.InquiriesRepository;
import com.bidnbuy.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiriesService {

    private final InquiriesRepository inquiriesRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    // 일반 문의 등록이므로 type = GENERAL
    public InquiryResponse createInquiry(Long userId, CreateInquiryRequest dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Inquiries inquiry = Inquiries.builder()
                .user(user)
                .admin(null) // 등록 시점에는 관리자가 배정되지 않음
                .type(InquiryEnums.InquiryType.GENERAL)
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(InquiryEnums.InquiryStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

        Inquiries saved = inquiriesRepository.save(inquiry);
        return InquiryResponse.fromEntity(saved);
    }
}
