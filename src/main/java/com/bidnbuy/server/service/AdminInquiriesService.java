package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AdminInquiryListDto;
import com.bidnbuy.server.dto.AdminInquiryDetailDto;
import com.bidnbuy.server.dto.AdminInquiryReplyRequestDto;
import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import com.bidnbuy.server.repository.InquiriesRepository;
import com.bidnbuy.server.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminInquiriesService {
    
    private final InquiriesRepository inquiriesRepository;
    private final AdminRepository adminRepository;

    // 문의 목록 조회
    public Page<AdminInquiryListDto> getInquiryList(Pageable pageable, InquiryEnums.InquiryType type, InquiryEnums.InquiryStatus status, String title, String userEmail) {
        log.info("관리자 문의 목록 조회 요청: page={}, size={}, type={}, status={}, title={}, userEmail={}", 
                pageable.getPageNumber(), pageable.getPageSize(), type, status, title, userEmail);
        
        Page<InquiriesEntity> inquiries;
        
        // 검색 조건 있는 경우
        if (title != null && !title.trim().isEmpty()) {
            // 제목 검색
            if (type != null && status != null) {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(title, type, status, pageable);
            } else if (type != null) {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(title, type, pageable);
            } else if (status != null) {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(title, status, pageable);
            } else {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title, pageable);
            }
        } else if (userEmail != null && !userEmail.trim().isEmpty()) {
            // 작성자(이메일) 검색
            if (type != null && status != null) {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(userEmail, type, status, pageable);
            } else if (type != null) {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(userEmail, type, pageable);
            } else if (status != null) {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(userEmail, status, pageable);
            } else {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(userEmail, pageable);
            }
        } else {
            // 검색 조건 없는 경우
            if (type != null && status != null) {
                // 타입+상태 필터링
                inquiries = inquiriesRepository.findByTypeAndStatusOrderByCreatedAtDesc(type, status, pageable);
            } else if (type != null) {
                // 타입
                inquiries = inquiriesRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
            } else if (status != null) {
                // 상태
                inquiries = inquiriesRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            } else {
                // 전체 조회
                inquiries = inquiriesRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }
        
        return inquiries.map(this::convertToInquiryListDto);
    }

    // 문의 상세 조회
    public AdminInquiryDetailDto getInquiryDetail(Long inquiryId) {
        log.info("관리자 문의 상세 조회 요청: inquiryId={}", inquiryId);
        
        InquiriesEntity inquiry = inquiriesRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));
        
        return AdminInquiryDetailDto.builder()
                .inquiriesId(inquiry.getInquiriesId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .type(inquiry.getType())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .updateAt(inquiry.getUpdateAt())
                .userId(inquiry.getUser().getUserId())
                .userEmail(inquiry.getUser().getEmail())
                .userNickname(inquiry.getUser().getNickname())
                .adminId(inquiry.getAdmin() != null ? inquiry.getAdmin().getAdminId() : null)
                .adminEmail(inquiry.getAdmin() != null ? inquiry.getAdmin().getEmail() : null)
                .requestTitle(inquiry.getRequestTitle())
                .requestContent(inquiry.getRequestContent())
                .build();
    }

    // 문의 답변
    @Transactional
    public void replyToInquiry(Long inquiryId, AdminInquiryReplyRequestDto request) {
        log.info("관리자 문의 답변 작성: inquiryId={}", inquiryId);
        
        InquiriesEntity inquiry = inquiriesRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));
        
        // 관리자 정보 설정
        // TODO 이거 어떻게 처리할지??
        AdminEntity admin = adminRepository.findById(1L) // 일단 첫번째 관리자 사용
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));
        
        inquiry.setAdmin(admin);
        inquiry.setRequestTitle(request.getTitle());
        inquiry.setRequestContent(request.getContent());
        inquiry.setStatus(InquiryEnums.InquiryStatus.COMPLETE); // 답변 시 자동으로 완료 상태 변경
        inquiry.setUpdateAt(LocalDateTime.now());
        
        inquiriesRepository.save(inquiry);
        log.info("문의 답변 작성 완료: inquiryId={}, 상태=COMPLETE", inquiryId);
    }

    // 문의 상태 변경
    @Transactional
    public void updateInquiryStatus(Long inquiryId, InquiryEnums.InquiryStatus status) {
        log.info("관리자 문의 상태 변경: inquiryId={}, status={}", inquiryId, status);
        
        InquiriesEntity inquiry = inquiriesRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));
        
        inquiry.setStatus(status);
        inquiry.setUpdateAt(LocalDateTime.now());
        
        inquiriesRepository.save(inquiry);
        log.info("문의 상태 변경 완료: inquiryId={}, status={}", inquiryId, status);
    }

    // InquiriesEntity -> AdminInquiryListDto
    private AdminInquiryListDto convertToInquiryListDto(InquiriesEntity inquiry) {
        return AdminInquiryListDto.builder()
                .inquiriesId(inquiry.getInquiriesId())
                .title(inquiry.getTitle())
                .type(inquiry.getType())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .userId(inquiry.getUser().getUserId())
                .userEmail(inquiry.getUser().getEmail())
                .userNickname(inquiry.getUser().getNickname())
                .build();
    }
}