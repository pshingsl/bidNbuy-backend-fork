package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AdminInquiryListDto;
import com.bidnbuy.server.dto.AdminInquiryDetailDto;
import com.bidnbuy.server.dto.AdminInquiryReplyRequestDto;
import com.bidnbuy.server.dto.PagingResponseDto;
import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.entity.AdminEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import com.bidnbuy.server.enums.NotificationType;
import com.bidnbuy.server.enums.UserStatus;
import com.bidnbuy.server.repository.InquiriesRepository;
import com.bidnbuy.server.repository.AdminRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
    private final UserNotificationService userNotificationService;
    private final UserRepository userRepository;

    private final EntityManager entityManager;

    // 삭제 유저 것도 포함시킴

    // 문의 목록 조회
    public PagingResponseDto<AdminInquiryListDto> getInquiryList(Pageable pageable, InquiryEnums.InquiryType type, InquiryEnums.InquiryStatus status, String title, String userEmail) {
        log.info("관리자 문의 목록 조회 요청: page={}, size={}, type={}, status={}, title={}, userEmail={}",
                pageable.getPageNumber(), pageable.getPageSize(), type, status, title, userEmail);

        Page<InquiriesEntity> inquiries;

        // 검색 조건 있는 경우
        if (title != null && !title.trim().isEmpty()) {
            // 제목 검색
            if (type != null && status != null) {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(title, type.name(), status.name(), pageable);
            } else if (type != null) {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(title, type.name(), pageable);
            } else if (status != null) {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(title, status.name(), pageable);
            } else {
                inquiries = inquiriesRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title, pageable);
            }
        } else if (userEmail != null && !userEmail.trim().isEmpty()) {
            // 작성자(이메일) 검색
            if (type != null && status != null) {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(userEmail, type.name(), status.name(), pageable);
            } else if (type != null) {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(userEmail, type.name(), pageable);
            } else if (status != null) {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(userEmail, status.name(), pageable);
            } else {
                inquiries = inquiriesRepository.findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(userEmail, pageable);
            }
        } else {
            // 검색 조건 없는 경우
            if (type != null && status != null) {
                // 타입+상태 필터링
                inquiries = inquiriesRepository.findByTypeAndStatusOrderByCreatedAtDesc(type.name(), status.name(), pageable);
            } else if (type != null) {
                // 타입
                inquiries = inquiriesRepository.findByTypeOrderByCreatedAtDesc(type.name(), pageable);
            } else if (status != null) {
                // 상태
                inquiries = inquiriesRepository.findByStatusOrderByCreatedAtDesc(status.name(), pageable);
            } else {
                // 전체 조회
                inquiries = inquiriesRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        // 네이티브쿼리 결과 받은 직후 모든 엔티티 detach, 관계 로딩 방지
        log.debug("네이티브 쿼리 결과 받음: totalElements={}", inquiries.getTotalElements());
        List<InquiriesEntity> inquiryList = inquiries.getContent();
        log.debug("getContent() 호출 완료: inquiryList.size()={}", inquiryList.size());
        
        for (InquiriesEntity inquiry : inquiryList) {
            entityManager.detach(inquiry);
        }
        log.debug("모든 엔티티 detach 완료");

        List<AdminInquiryListDto> dtoList = inquiryList.stream()
                .map(this::convertToInquiryListDto)
                .collect(Collectors.toList());
        log.debug("DTO 변환 완료: dtoList.size()={}", dtoList.size());

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

        InquiriesEntity inquiry = inquiriesRepository.findByIdIncludingDeletedUser(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));
        
        // 네이티브쿼리 결과 detach..
        entityManager.detach(inquiry);

        Long userIdSafe = null;
        String userEmailSafe = null;
        String userNicknameSafe = null;
        try {
            Long userIdFromDb = inquiriesRepository.findUserIdByInquiryIdNative(inquiryId);
            if (userIdFromDb != null) {
                UserEntity user = userRepository.findByIdIncludingDeleted(userIdFromDb).orElse(null);
                if (user != null) {
                    userIdSafe = user.getUserId();
                    userEmailSafe = user.getEmail();
                    // 삭제 유저면 "탈퇴회원" 표시
                    userNicknameSafe = (user.getDeletedAt() != null) ? "탈퇴회원" : user.getNickname();
                }
            }
        } catch (Exception e) {
            log.warn("Inquiry detail: user information retrieval failed. inquiryId={}, error={}", inquiryId, e.getMessage());
        }

        // adminId조회
        Long adminIdSafe = null;
        try {
            adminIdSafe = inquiriesRepository.findAdminIdByInquiryIdNative(inquiryId);
        } catch (Exception e) {
            log.warn("Inquiry detail: admin information retrieval failed. inquiryId={}, error={}", inquiryId, e.getMessage());
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
                .adminId(adminIdSafe)
                .requestTitle(inquiry.getRequestTitle())
                .requestContent(inquiry.getRequestContent())
                .build();
    }

    // 문의 답변
    @Transactional
    public void replyToInquiry(Long inquiryId, AdminInquiryReplyRequestDto request, Long adminId) {
        log.info("관리자 문의 답변 작성: inquiryId={}, adminId={}", inquiryId, adminId);

        InquiriesEntity inquiry = inquiriesRepository.findByIdIncludingDeletedUser(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId));

        Long userIdFromDb = inquiriesRepository.findUserIdByInquiryIdNative(inquiryId);
        if (userIdFromDb == null) {
            throw new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다: " + inquiryId);
        }
        
        UserEntity user = userRepository.findByIdIncludingDeleted(userIdFromDb).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("문의 작성자 정보를 찾을 수 없습니다: " + inquiryId);
        }

        // 삭제 유저 문의는 답변 불가
        if (user.getDeletedAt() != null || user.getUserStatus() == UserStatus.B || user.getBanCount() > 0) {
            throw new IllegalArgumentException("삭제된 사용자 문의에는 답변할 수 없습니다.");
        }
        
        // 수정 작업 위해 user 필드 복원
        inquiry.setUser(user);

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

        // 문의 작성자에게 알림 발송 (탈퇴 회원인 경우x)
        try {
            String content = "문의하신 내용에 대한 답변이 등록되었습니다.";
            userNotificationService.createNotification(user.getUserId(), NotificationType.NOTICE, content);
            log.info("문의 답변 알림 전송 완료: userId={}, inquiryId={}", user.getUserId(), inquiryId);
        } catch (Exception e) {
            log.warn("문의 답변 알림 전송 실패: {}", e.getMessage());
        }
    }

    // 문의 상태 변경
    @Transactional
    public void updateInquiryStatus(Long inquiryId, InquiryEnums.InquiryStatus status) {
        log.info("관리자 문의 상태 변경: inquiryId={}, status={}", inquiryId, status);

        // 부분 업데이트로 상태 변경
        int changed = inquiriesRepository.updateStatusOnly(inquiryId, status, LocalDateTime.now());
        if (changed == 0) {
            throw new IllegalArgumentException("문의를 찾을 수 없습니다: " + inquiryId);
        }
        
        log.info("문의 상태 변경 완료: inquiryId={}, status={}", inquiryId, status);
    }

    // InquiriesEntity -> AdminInquiryListDto
    private AdminInquiryListDto convertToInquiryListDto(InquiriesEntity inquiry) {
        Long userIdSafe = null;
        String userEmailSafe = null;
        String userNicknameSafe = null;
        try {
            Long userIdFromDb = inquiriesRepository.findUserIdByInquiryIdNative(inquiry.getInquiriesId());
            if (userIdFromDb != null) {
                UserEntity user = userRepository.findByIdIncludingDeleted(userIdFromDb).orElse(null);
                if (user != null) {
                    userIdSafe = user.getUserId();
                    userEmailSafe = user.getEmail();
                    userNicknameSafe = (user.getDeletedAt() != null) ? "탈퇴회원" : user.getNickname();
                }
            }
        } catch (Exception e) {
            log.warn("Inquiry list: user information retrieval failed. inquiryId={}, error={}", inquiry.getInquiriesId(), e.getMessage());
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