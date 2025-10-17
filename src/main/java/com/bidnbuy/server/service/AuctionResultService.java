package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AuctionPurchaseHistoryDto;
import com.bidnbuy.server.dto.AuctionSalesHistoryDto;
import com.bidnbuy.server.dto.MyPageSummaryDto;
import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.enums.TradeFilterStatus;
import com.bidnbuy.server.repository.AuctionResultRepository;
import com.bidnbuy.server.repository.ImageRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionResultService {
    private final AuctionResultRepository auctionResultRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    // 마이페이지 기본
    @Transactional(readOnly = true)
    public MyPageSummaryDto getMyPageSummaryDto(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 마이페이지 최근
        List<AuctionPurchaseHistoryDto> recentPurchases = getRecentPurchases(userId); // 구매
        List<AuctionSalesHistoryDto> recentSales = getRecentSales(userId); // 판매
        
        // TODO 아직 리뷰 받는 게 없어서 온도 하드코딩 추후에 완료되면 적용
        Double temperature = 36.5;

        return MyPageSummaryDto.builder().nickname(user.getNickname())
                .temperature(temperature)
                .profileImageUrl(user.getProfileImageUrl())
                .recentPurchases(recentPurchases)
                .recentSales(recentSales)
                .build();
    }

    @Transactional(readOnly = true)
    private List<AuctionPurchaseHistoryDto> getRecentPurchases(Long userId) {

        List<AuctionPurchaseHistoryDto> history = getPurchaseHistory(userId, TradeFilterStatus.ALL);

        if (history.size() > 3) {
            return history.subList(0, 3);
        }
        return history;
    }

    @Transactional(readOnly = true)
    private List<AuctionSalesHistoryDto> getRecentSales(Long userId) {

        List<AuctionSalesHistoryDto> history = getSalesHistory(userId, TradeFilterStatus.ALL);

        if (history.size() > 3) {
            return history.subList(0, 3);
        }
        return history;
    }

    // 마이페이지 - 구매내역  (낙찰 내역) 조회
    @Transactional(readOnly = true)
    public List<AuctionPurchaseHistoryDto> getPurchaseHistory(Long userId, TradeFilterStatus filterStatus) {
        List<AuctionResultEntity> results = auctionResultRepository.findByWinner_UserId_Optimized(userId);

        return results.stream()
                .filter(result -> isMatchingStatus(result.getResultStatus(), filterStatus))
                .map(this::toPurchaseDto)
                .collect(Collectors.toList());
    }

    // 마이페이지 - 판매 내역 (판매 결과) 조회
    public List<AuctionSalesHistoryDto> getSalesHistory(Long userId, TradeFilterStatus filterStatus) {
        // 판매자(Auction_User) 기준으로 조회합니다.
        List<AuctionResultEntity> results = auctionResultRepository.findByAuction_User_UserId_Optimized(userId);

        return results.stream()
                .filter(result -> isMatchingStatus(result.getResultStatus(), filterStatus))
                .map(this::toSalesDto)
                .collect(Collectors.toList());
    }

    // 구매 내역
    private AuctionPurchaseHistoryDto toPurchaseDto(AuctionResultEntity result) {
        String status = determineStatusText(result);

        Long auctionId = result.getAuction().getAuctionId();
        String imageUrl = imageRepository.findFirstImageUrlByAuctionId(auctionId)
                .orElse("/images/default_product.png");

        return AuctionPurchaseHistoryDto.builder()
                .auctionId(result.getAuction().getAuctionId())
                .title(result.getAuction().getTitle())
                .itemImageUrl(imageUrl)
                .sellerNickname(result.getAuction().getUser().getNickname())
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
        if (result.getWinner() != null) {
            winnerNickname = result.getWinner().getNickname();
        }

        return AuctionSalesHistoryDto.builder()
                .auctionId(result.getAuction().getAuctionId())
                .title(result.getAuction().getTitle())
                .itemImageUrl(imageUrl)
                .startTime(result.getAuction().getStartTime())
                .endTime(result.getAuction().getEndTime())
                .statusText(statusText)
                .finalPrice(result.getFinalPrice())
                .winnerNickname(winnerNickname)
                .build();
    }

    private boolean isMatchingStatus(ResultStatus resultStatus, TradeFilterStatus filterStatus) {
        if (filterStatus == TradeFilterStatus.ALL) {
            return true;
        }

        return switch (filterStatus) {
            case ONGOING ->
                // 진행 중: 결제 대기 중인 상태만 해당
                    resultStatus == ResultStatus.SUCCESS_PENDING_PAYMENT;

            case COMPLETED ->
                // 완료: 거래 완료 상태만 해당
                    resultStatus == ResultStatus.SUCCESS_COMPLETED;

            case CANCELLED ->
                // 취소/실패: 유찰 또는 거래 취소 상태만 해당
                    resultStatus == ResultStatus.FAILURE || resultStatus == ResultStatus.CANCELED;

            default -> false; // 정의되지 않은 상태
        };
    }

    // 구매내역에 페이별로 조회
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
}
