package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AdminUserListDto;
import com.bidnbuy.server.dto.AdminUserDetailDto;
import com.bidnbuy.server.dto.PagingResponseDto;
import com.bidnbuy.server.dto.PenaltyHistoryDto;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.PenaltyEntity;
import com.bidnbuy.server.repository.UserRepository;
import com.bidnbuy.server.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminUserService {
    
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;

    // 회원 목록 조회 (페이징)
    public PagingResponseDto<AdminUserListDto> getUserList(Pageable pageable, String email) {
        log.info("회원 목록 조회 요청: page={}, size={}, email={}", 
                pageable.getPageNumber(), pageable.getPageSize(), email);
        
        List<UserEntity> allUsers;
        
        if (email != null && !email.isEmpty()) {
            allUsers = userRepository.findByEmailContainingIgnoreCaseIncludingDeleted(email);
        } else {
            allUsers = userRepository.findAllIncludingDeleted();
        }
        
        // 수동으로 페이징...
        int totalElements = allUsers.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startIdx = currentPage * pageSize;
        int endIdx = Math.min(startIdx + pageSize, totalElements);
        
        List<UserEntity> pagedUsers = allUsers.subList(
                Math.min(startIdx, totalElements),
                Math.min(endIdx, totalElements)
        );
        
        List<AdminUserListDto> dtoList = pagedUsers.stream()
                .map(this::convertToUserListDto)
                .collect(Collectors.toList());
        
        int totalPages = (totalElements + pageSize - 1) / pageSize;
        boolean isFirst = currentPage == 0;
        boolean isLast = currentPage >= totalPages - 1 || totalElements == 0;
        
        return PagingResponseDto.<AdminUserListDto>builder()
                .data(dtoList)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .isFirst(isFirst)
                .isLast(isLast)
                .build();
    }

    // 회원 상세 조회
    public AdminUserDetailDto getUserDetail(Long userId) {
        log.info("회원 상세 조회 요청: userId={}", userId);
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
        List<PenaltyEntity> penaltyEntities = penaltyRepository.findByUserOrderByCreatedAtDesc(user);
        
        // PenaltyEntity -> PenaltyHistoryDto
        List<PenaltyHistoryDto> penaltyHistory = penaltyEntities.stream()
                .map(this::convertToPenaltyHistoryDto)
                .collect(Collectors.toList());
        
        // 거래글 카운트
        int auctionCount = user.getAuctionProducts().size();
        
        return AdminUserDetailDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .penaltyPoints(user.getPenaltyPoints())
                .activityStatus(getActivityStatus(user))
                .isSuspended(user.isSuspended())
                .suspendedUntil(user.getSuspendedUntil())
                .suspensionCount(user.getSuspensionCount())
                .banCount(user.getBanCount())
                .auctionCount(auctionCount)
                .penaltyHistory(penaltyHistory)
                .userType(user.getUserType())
                .userTemperature(user.getUserTemperature())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    // UserEntity -> AdminUserListDto
    private AdminUserListDto convertToUserListDto(UserEntity user) {
        return AdminUserListDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .penaltyPoints(user.getPenaltyPoints())
                .activityStatus(getActivityStatus(user))
                .isSuspended(user.isSuspended())
                .suspendedUntil(user.getSuspendedUntil())
                .build();
    }

    // PenaltyEntity -> PenaltyHistoryDto
    private PenaltyHistoryDto convertToPenaltyHistoryDto(PenaltyEntity penalty) {
        return PenaltyHistoryDto.builder()
                .penaltyId(penalty.getPenaltyId())
                .type(penalty.getType())
                .points(penalty.getPoints())
                .createdAt(penalty.getCreatedAt())
                .isActive(penalty.isActive())
                .build();
    }

    // 활동 상태 계산
    private String getActivityStatus(UserEntity user) {
        if (user.getDeletedAt() != null) {
            return "강퇴";
        } else if (user.isSuspended()) {
            return "정지";
        } else {
            return "활동";
        }
    }
}