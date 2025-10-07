package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AuctionPurchaseHistoryDto;
import com.bidnbuy.server.dto.AuctionSalesHistoryDto;
import com.bidnbuy.server.entity.AuctionResultEntity;
import com.bidnbuy.server.enums.ResultStatus;
import com.bidnbuy.server.repository.AuctionResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionResultService {
    private final AuctionResultRepository auctionResultRepository;


    // 마이페이지 - 구매내역  (낙찰 내역) 조회
    @Transactional(readOnly = true)
    public List<AuctionPurchaseHistoryDto> getPurchaseHistory(Long userId) {
        List<AuctionResultEntity> results = auctionResultRepository.findByWinner_UserId_Optimized(userId);

        return  results.stream()
                .map(this::toPurchaseDto)
                .collect(Collectors.toList());
    }

    // 마이페이지 - 판매 내역 (판매 결과) 조회
    public List<AuctionSalesHistoryDto> getSalesHistory(Long userId) {
        // 판매자(Auction_User) 기준으로 조회합니다.
        List<AuctionResultEntity> results = auctionResultRepository.findByAuction_User_UserId_Optimized(userId);

        return results.stream()
                .map(this::toSalesDto)
                .collect(Collectors.toList());
    }

    // 구매 내역
    private AuctionPurchaseHistoryDto toPurchaseDto(AuctionResultEntity result) {
        String status = determineStatusText(result);

        String imageUrl = "이미지";

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
        String imageUrl = "이미지";

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


    // 구매내역에 페이별로 조회
    private String determineStatusText(AuctionResultEntity result) {
        ResultStatus status = result.getResultStatus();

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

        return  "상태 정보 없음";
    }
}
