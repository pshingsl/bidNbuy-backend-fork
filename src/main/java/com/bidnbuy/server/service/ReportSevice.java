package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.CreateReportRequestDto;
import com.bidnbuy.server.dto.ReportResponseDto;
import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import com.bidnbuy.server.repository.InquiriesRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportSevice {

    private final UserRepository userRepository;
    private final InquiriesRepository inquiriesRepository;

    // 상세 조회
    @Transactional
    public ReportResponseDto getReportDetail(Long reportId, Long userId) {
        InquiriesEntity report = inquiriesRepository.findByInquiriesIdAndType(reportId, InquiryEnums.InquiryType.REPORT)
                .orElseThrow(() -> new EntityNotFoundException("해당 신고를 찾을 수 없습니다."));

        // 🔒 본인 신고가 아닌 경우 예외
        if (report.getUser().getUserId()!= userId) {
            throw new SecurityException("본인이 등록한 신고만 조회할 수 있습니다.");
        }

        return ReportResponseDto.builder()
                .reportId(report.getInquiriesId())
                .title(report.getTitle())
                .content(report.getContent())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .requestTitle(report.getRequestTitle())
                .requestContent(report.getRequestContent())
                .build();
    }


    // 전체 조회
    @Transactional
    public List<ReportResponseDto> getMyReports(Long userId) {
        return inquiriesRepository.findByUser_UserIdAndType(userId, InquiryEnums.InquiryType.REPORT)
                .stream()
                .map(r -> ReportResponseDto.builder()
                        .reportId(r.getInquiriesId())
                        .title(r.getTitle())
                        .content(r.getContent())
                        .status(r.getStatus().name())
                        .createdAt(r.getCreatedAt())
                        .build()
                )
                .toList();
    }

    // 등록
    public InquiriesEntity createReport(Long userId, CreateReportRequestDto request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        InquiriesEntity report = InquiriesEntity.builder()
                .user(user)
                .type(InquiryEnums.InquiryType.REPORT) // 신고 타입 고정
                .title(request.getTitle())
                .content(request.getContent())
                .status(InquiryEnums.InquiryStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

        return inquiriesRepository.save(report);
    }
}
