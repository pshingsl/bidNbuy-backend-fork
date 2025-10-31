package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AdminInquiryListDto;
import com.bidnbuy.server.dto.AdminInquiryDetailDto;
import com.bidnbuy.server.dto.AdminInquiryReplyRequestDto;
import com.bidnbuy.server.dto.PagingResponseDto;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminInquiriesService {
    
    private final InquiriesRepository inquiriesRepository;
    private final AdminRepository adminRepository;

    // 문의 목록 조회
    public PagingResponseDto<AdminInquiryListDto> getInquiryList(Pageable pageable, InquiryEnums.InquiryType type, InquiryEnums.InquiryStatus status, String title, String userEmail) {
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
        
        List<AdminInquiryListDto> dtoList = inquiries.getContent().stream()
                .map(this::convertToInquiryListDto)
                .collect(Collectors.toList());
        
        return PagingResponseDto.<AdminInquiryListDto>builder()
                .data(dtoList)
                .totalPages(inquiries.getTotalPages())
                .totalElements(inquiries.getTotalElements())
                .currentPage(inquiries.getNumber())
                .pageSize(inquiries.getSize())
                .isFirst(inquiries.isFirst())
                .isLast(inquiries.isLast())
                .build();
    }

    // 문의 상세 조회
    public AdminInquiryDetailDto getInquiryDetail(Long inquiryId) {
        log.info("관리자 문의 상세 조회 요청: inquiryId={}", inquiryId);
        
        InquiriesEntity inquiry = inquiriesRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));
        
        Long userIdSafe = null;
        String userEmailSafe = null;
        String userNicknameSafe = "탈퇴회원";
        try {
            if (inquiry.getUser() != null) {
                userIdSafe = inquiry.getUser().getUserId();
                userEmailSafe = inquiry.getUser().getEmail();
                userNicknameSafe = inquiry.getUser().getNickname();
            }
        } catch (Exception e) {
            log.warn("Inquiry detail: user reference not available (possibly deleted). inquiryId={}", inquiryId);
        }

        return AdminInquiryDetailDto.builder()
                .inquiriesId(inquiry.getInquiriesId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .type(inquiry.getType())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .updateAt(inquiry.getUpdateAt())
                .userId(userIdSafe)
                .userEmail(userEmailSafe)
                .userNickname(userNicknameSafe)
                .adminId(inquiry.getAdmin() != null ? inquiry.getAdmin().getAdminId() : null)
                .requestTitle(inquiry.getRequestTitle())
                .requestContent(inquiry.getRequestContent())
                .build();
    }

    // 문의 답변
    @Transactional
    public void replyToInquiry(Long inquiryId, AdminInquiryReplyRequestDto request, Long adminId) {
        log.info("관리자 문의 답변 작성: inquiryId={}, adminId={}", inquiryId, adminId);
        
        InquiriesEntity inquiry = inquiriesRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));
        
        // 관리자 set
        AdminEntity admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다: " + adminId));
        
        inquiry.setAdmin(admin);
        inquiry.setRequestTitle(request.getTitle());
        inquiry.setRequestContent(request.getContent());
        inquiry.setStatus(InquiryEnums.InquiryStatus.COMPLETE); // 답변 시 자동으로 완료 상태 변경
        inquiry.setUpdateAt(LocalDateTime.now());
        
        inquiriesRepository.save(inquiry);
        log.info("문의 답변 작성 완료: inquiryId={}, adminId={}, 상태=COMPLETE", inquiryId, adminId);
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
        Long userIdSafe = null;
        String userEmailSafe = null;
        String userNicknameSafe = "탈퇴회원";
        try {
            if (inquiry.getUser() != null) {
                userIdSafe = inquiry.getUser().getUserId();
                userEmailSafe = inquiry.getUser().getEmail();
                userNicknameSafe = inquiry.getUser().getNickname();
            }
        } catch (Exception e) {
            log.warn("Inquiry list: user reference not available (possibly deleted). inquiryId={}", inquiry.getInquiriesId());
        }

        return AdminInquiryListDto.builder()
                .inquiriesId(inquiry.getInquiriesId())
                .title(inquiry.getTitle())
                .type(inquiry.getType())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .userId(userIdSafe)
                .userEmail(userEmailSafe)
                .userNickname(userNicknameSafe)
                .build();
    }
}