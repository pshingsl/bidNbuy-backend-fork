package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.*;
import com.bidnbuy.server.entity.*;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.enums.TradeFilterStatus;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.AuctionResultRepository;
import com.bidnbuy.server.repository.ImageRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionResultService {
    private final AuctionResultRepository auctionResultRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final AuctionProductsRepository auctionProductsRepository;

    // 1. 마이페이지 기본
    @Transactional(readOnly = true)
    public MyPageSummaryDto getMyPageSummaryDto(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 마이페이지 최근 구매
        List<AuctionPurchaseHistoryDto> recentPurchases = getRecentPurchases(userId); // 구매

        // 마이페이지 최근 판매
        List<AuctionSalesHistoryDto> recentSales = getRecentSales(userId); // 판매

        // 온도
        Double temperature = user.getUserTemperature();

        return MyPageSummaryDto.builder().nickname(user.getNickname())
                .temperature(temperature)
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .recentPurchases(recentPurchases)
                .recentSales(recentSales)
                .build();
    }

    // 2. 마이 페이지 구매내역
    @Transactional(readOnly = true)
    private List<AuctionPurchaseHistoryDto> getRecentPurchases(Long userId) {

        List<AuctionResultEntity> recentResults =
                auctionResultRepository.findTop3ByWinnerOrderByOrderUpdatedAtDesc(userId);

        // DTO로 변환하여 반환
        return recentResults.stream()
                .map(result -> toPurchaseDto(result)) // toPurchaseDto 사용
                .collect(Collectors.toList());
    }

    // 3. 마이 페이지 판매내역
    @Transactional(readOnly = true)
    private List<AuctionSalesHistoryDto> getRecentSales(Long userId) {

        // 판매 중 또는 시작 전 상태만 조회
        List<SellingStatus> activeStatuses = List.of(
                SellingStatus.BEFORE,
                SellingStatus.SALE,
                SellingStatus.PROGRESS
        );

        List<AuctionProductsEntity> activeAuctions =
                auctionProductsRepository.findTop3ByUser_UserIdAndSellingStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(
                        userId,
                        activeStatuses
                );

        return activeAuctions.stream()
                .map(this::toActiveSalesDto)
                .collect(Collectors.toList());
    }

    // 마이페이지 - 구매내역(필터링)
    @Transactional(readOnly = true)
    public List<AuctionPurchaseHistoryDto> getPurchaseHistory(Long userId, TradeFilterStatus filterStatus) {
        List<AuctionResultEntity> results = auctionResultRepository.findByWinner_UserId_Optimized(userId);

        return results.stream()
                .filter(result -> isMatchingStatus(result.getResultStatus(), filterStatus))
                .map(this::toPurchaseDto)
                .collect(Collectors.toList());
    }

    // 마이페이지 - 판매 내역(필터 적용)
    public List<AuctionSalesHistoryDto> getSalesHistory(Long userId, TradeFilterStatus filterStatus) {
        Set<AuctionSalesHistoryDto> salesHistorySet = new HashSet<>();

        // 경매 진행 중 / 시작전 필터링
        if (filterStatus == TradeFilterStatus.ONGOING || filterStatus == TradeFilterStatus.ALL) {
            List<SellingStatus> ongoingStatuses = List.of(SellingStatus.BEFORE, SellingStatus.SALE, SellingStatus.PROGRESS);

            List<AuctionProductsEntity> activeAuctions =
                    auctionProductsRepository.findByUser_UserIdAndSellingStatusInAndDeletedAtIsNull(
                            userId,
                            ongoingStatuses
                    );

            List<AuctionSalesHistoryDto> activeSales = activeAuctions.stream()
                    .map(this::toActiveSalesDto)
                    .collect(Collectors.toList());

            if (filterStatus == TradeFilterStatus.ONGOING) {
                return activeSales;
            }
            salesHistorySet.addAll(activeSales);
        }

        // 2. 경매 종료 후 거래 상태 필터링 (ResultStatus 사용)
        if (filterStatus == TradeFilterStatus.COMPLETED || filterStatus == TradeFilterStatus.CANCELLED || filterStatus == TradeFilterStatus.ALL) {

            List<AuctionResultEntity> results = auctionResultRepository.findByAuction_User_UserId_Optimized(userId);

            List<AuctionSalesHistoryDto> completedSales = results.stream()
                    .filter(result -> {
                        // ALL이거나, COMPLETED/CANCELLED 필터일 때만 ResultStatus 매칭
                        if (filterStatus == TradeFilterStatus.ALL) {
                            return result.getResultStatus() != null;
                        }
                        return isMatchingStatus(result.getResultStatus(), filterStatus);
                    })
                    .map(this::toSalesDto)
                    .collect(Collectors.toList());

            salesHistorySet.addAll(completedSales);
        }

        // 3. 필터가 COMPLETED/CANCELLED인 경우 (1번 로직을 타지 않음)
        if (filterStatus == TradeFilterStatus.COMPLETED || filterStatus == TradeFilterStatus.CANCELLED) {
            return new ArrayList<>(salesHistorySet);
        }

        return new ArrayList<>(salesHistorySet);
    }

    // 구매 내역
    private AuctionPurchaseHistoryDto toPurchaseDto(AuctionResultEntity result) {
        String status = determineStatusText(result);

        Long auctionId = result.getAuction().getAuctionId();
        String imageUrl = imageRepository.findFirstImageUrlByAuctionId(auctionId)
                .orElse("/images/default_product.png");

        String sellerNicknameSafe = "탈퇴회원";
        try {
            if (result.getAuction() != null && result.getAuction().getUser() != null) {
                sellerNicknameSafe = result.getAuction().getUser().getNickname();
            }
        } catch (Exception e) {
            // ignore, use fallback
        }

        return AuctionPurchaseHistoryDto.builder()
                .auctionId(result.getAuction().getAuctionId())
                .title(result.getAuction().getTitle())
                .itemImageUrl(imageUrl)
                .sellerNickname(sellerNicknameSafe)
                .endTime(result.getAuction().getEndTime())
                .status(status)
                .build();
    }

    // 판매 내역
    private AuctionSalesHistoryDto toSalesDto(AuctionResultEntity result) {
        String statusText = determineStatusText(result);

        Long auctionId = result.getAuction().getAuctionId();
        String imageUrl = imageRepository.findFirstImageUrlByAuctionId(auctionId)
                .orElse("/images/default_product.png");

        String winnerNickname = null;
        try {
            if (result.getWinner() != null) {
                winnerNickname = result.getWinner().getNickname();
            }
        } catch (Exception e) {
            // keep null
        }
        // 여기 추가: ORDER -> ADDRESS 꺼내기
        OrderEntity order = result.getOrder();
        AddressEntity addr = (order != null) ? order.getShippingAddress() : null;

        String recipientName = (addr != null) ? addr.getRecipientName() : null;
        String phoneNumber = (addr != null) ? addr.getPhoneNumber() : null;
        String zonecode = (addr != null) ? addr.getZonecode() : null;
        String addressValue = (addr != null) ? addr.getAddress() : null;
        String detailAddress = (addr != null) ? addr.getDetailAddress() : null;


        return AuctionSalesHistoryDto.builder()
                .auctionId(result.getAuction().getAuctionId())
                .title(result.getAuction().getTitle())
                .itemImageUrl(imageUrl)
                .startTime(result.getAuction().getStartTime())
                .endTime(result.getAuction().getEndTime())
                .statusText(statusText)
                .finalPrice(result.getFinalPrice())
                .winnerNickname(winnerNickname)

                // 여기서 주소 값들 세팅
                .recipientName(recipientName)
                .phoneNumber(phoneNumber)
                .zonecode(zonecode)
                .address(addressValue)
                .detailAddress(detailAddress)

                .build();
    }

    private boolean isMatchingStatus(ResultStatus resultStatus, TradeFilterStatus filterStatus) {
        if (filterStatus == TradeFilterStatus.ALL) {
            return true;
        }

        return switch (filterStatus) {

            // 진행 중: 결제 대기 중인 상태만 해당
            case ONGOING ->
                    resultStatus == ResultStatus.SUCCESS_PENDING_PAYMENT || resultStatus == ResultStatus.SUCCESS_PAID;

            // 완료: 거래 완료 상태만 해당
            case COMPLETED -> resultStatus == ResultStatus.SUCCESS_COMPLETED;

            // 취소/실패: 유찰 또는 거래 취소 상태만 해당
            case CANCELLED -> resultStatus == ResultStatus.FAILURE || resultStatus == ResultStatus.CANCELED;

            default -> false; // 정의되지 않은 상태
        };
    }

    // 11/16
    @Transactional(readOnly = true)
    public UserSalesListResponseDto getOtherUserSalesList(Long userId) {
        // 1. 판매 완료된 거래 (AuctionResultEntity 활용)
        // 경매 결과가 있으며, ResultStatus가 SUCCESS_COMPLETED인 것만 필터링합니다.
        List<AuctionResultEntity> completedResults = auctionResultRepository.findByAuction_User_UserId_Optimized(userId)
                .stream()
                .filter(result -> result.getResultStatus() == ResultStatus.SUCCESS_COMPLETED)
                .toList();

        List<OtherUserSalesDto> completedSales = completedResults.stream()
                .map(this::toOtherUserSalesDto) // 아래에 구현할 매핑 메서드
                .collect(Collectors.toList());

        // 2. 판매 중인 물품 (AuctionProductsEntity 활용)
        List<SellingStatus> ongoingStatuses = List.of(SellingStatus.BEFORE, SellingStatus.SALE, SellingStatus.PROGRESS);

        List<AuctionProductsEntity> activeAuctions =
                auctionProductsRepository.findByUser_UserIdAndSellingStatusInAndDeletedAtIsNull(
                        userId,
                        ongoingStatuses
                );

        List<OtherUserSalesDto> onSale = activeAuctions.stream()
                .map(this::toOtherUserSalesDto) // 아래에 구현할 매핑 메서드
                .collect(Collectors.toList());

        // 3. UserSalesListResponseDto로 취합
        return UserSalesListResponseDto.builder()
                .completedSalesCount(completedSales.size())
                .completedSales(completedSales)
                .onSaleCount(onSale.size())
                .onSale(onSale)
                .build();
    }

    // ✨ 5. OtherUserSalesDto 변환을 위한 매핑 메서드 추가

    // 5-1. 판매 완료된 AuctionResultEntity를 OtherUserSalesDto로 변환
    private OtherUserSalesDto toOtherUserSalesDto(AuctionResultEntity result) {
        AuctionProductsEntity auction = result.getAuction();
        String imageUrl = imageRepository.findFirstImageUrlByAuctionId(auction.getAuctionId())
                .orElse("/images/default_product.png");

        return OtherUserSalesDto.builder()
                .auctionId(auction.getAuctionId())
                .title(auction.getTitle())
                .itemImageUrl(imageUrl)
               // .finalPrice(result.getFinalPrice())
                // 판매 완료는 경매 종료 시간이 기준이 됩니다.
             //   .endTime(auction.getEndTime())
                // 판매 완료 상태 텍스트
              //  .statusText(determineSalesStatusText(result))
                .build();
    }

    // 5-2. 판매 중인 AuctionProductsEntity를 OtherUserSalesDto로 변환
    private OtherUserSalesDto toOtherUserSalesDto(AuctionProductsEntity auction) {
        String imageUrl = imageRepository.findFirstImageUrlByAuctionId(auction.getAuctionId())
                .orElse("/images/default_product.png");

        return OtherUserSalesDto.builder()
                .auctionId(auction.getAuctionId())
                .title(auction.getTitle())
                .itemImageUrl(imageUrl)
                // 판매 중이므로 최종 가격은 null
                // .finalPrice(null)
               // .endTime(auction.getEndTime())
                // 판매 중 상태 텍스트
                //.statusText(auction.getSellingStatus().name())
                .build();
    }

    // 5-3. 판매 완료된 거래에 대한 상태 텍스트를 결정하는 헬퍼 메서드 (간단하게)
    private String determineSalesStatusText(AuctionResultEntity result) {
        // ResultStatus가 SUCCESS_COMPLETED 인 경우만 넘어온다고 가정
        if (result.getResultStatus() == ResultStatus.SUCCESS_COMPLETED) {
            return "판매 완료";
        }
        // 다른 상태라면 해당 텍스트를 반환하거나, 에러 처리
        return "상태 정보 없음";
    }

    // 상태 메서드
    private AuctionSalesHistoryDto toActiveSalesDto(AuctionProductsEntity auction) {

        String imageUrl = imageRepository.findFirstImageUrlByAuctionId(auction.getAuctionId())
                .orElse("/images/default_product.png");

        String statusText = auction.getSellingStatus().name();

        return AuctionSalesHistoryDto.builder()
                .auctionId(auction.getAuctionId())
                .title(auction.getTitle())
                .itemImageUrl(imageUrl)
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .statusText(statusText)
                .finalPrice(auction.getCurrentPrice())
                .winnerNickname(null)
                .build();
    }


    // 구매내역에 상태
    private String determineStatusText(AuctionResultEntity result) {
        ResultStatus status = result.getResultStatus();

        // 이건 의논해서 물어봐야함
        if (status == ResultStatus.FAILURE) {
            return "유찰 (종료)";
        } else if (status == ResultStatus.CANCELED) {
            return "거래 취소";
        } else if (status == ResultStatus.SUCCESS_COMPLETED) {
            return "거래 완료";
        } else if (status == ResultStatus.SUCCESS_PENDING_PAYMENT) {
            // 낙찰은 되었으나 결제/거래가 진행 중인 상태
            return "결제 대기 중 (진행 중)";
        }

        return "상태 정보 없음";
    }


    @Transactional(readOnly = true)
    public List<AuctionSalesHistoryDto> getPurchaseHistoryByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("endTime").descending());
        List<AuctionSalesHistoryDto> history = auctionResultRepository.findPurchaseHistoryByUserId(userId, pageable);

        // 상태 텍스트 가공
        return history.stream()
                .map(dto -> AuctionSalesHistoryDto.builder()
                        .auctionId(dto.getAuctionId())
                        .title(dto.getTitle())
                        .itemImageUrl(dto.getItemImageUrl())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .finalPrice(dto.getFinalPrice())
                        .winnerNickname(dto.getWinnerNickname())
                        .statusText(convertStatusToText(dto.getStatusText())) // 문자열 변환
                        .build()
                ).toList();
    }

    private String convertStatusToText(String resultStatus) {
        return switch (resultStatus) {
            case "SUCCESS" -> "낙찰 완료";
            case "CANCELLED" -> "유찰";
            default -> "진행중";
        };
    }
}
