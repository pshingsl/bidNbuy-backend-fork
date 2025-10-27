package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AuctionHistoryDto;
import com.bidnbuy.server.entity.AuctionHistoryEntity;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.enums.AuctionStatus;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.AuctionHistoryRepository;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuctionHistoryService {

    private final AuctionHistoryRepository historyRepository;
    private final AuctionProductsRepository auctionProductsRepository;

    // 상태 변경 이력 기록
    @Transactional
    public void recordStatusChange(Long auctionId, AuctionStatus newStatus) {
        AuctionProductsEntity auctionProduct = auctionProductsRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경매 상품을 찾을 수 없습니다."));

        AuctionStatus previousStatus = auctionProduct.getSellingStatus() != null
                ? mapToAuctionStatus(auctionProduct.getSellingStatus())
                : null;

        AuctionHistoryEntity history = AuctionHistoryEntity.builder()
                .auctionProduct(auctionProduct)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .bidTime(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    // 이력 조회
    @Transactional(readOnly = true)
    public List<AuctionHistoryDto> getHistory(
            Long auctionId,
            Long userId) {
        List<AuctionHistoryEntity> historyList =
                historyRepository.findAllByAuctionProduct_AuctionIdOrderByBidTimeAsc(auctionId);

        return historyList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AuctionHistoryDto convertToDto(AuctionHistoryEntity entity) {
        return AuctionHistoryDto.builder()
                .previousStatus(entity.getPreviousStatus())
                .newStatus(entity.getNewStatus())
                .bidTime(entity.getBidTime())
                .statusDescription(getStatusDescription(entity.getNewStatus()))
                .build();
    }

    private String getStatusDescription(AuctionStatus status) {
        return switch (status) {
            case PROGRESS -> "경매 진행 중";
            case FINISHED -> "경매 낙찰자 결정";
            case PAYMENT_PENDING -> "결제 대기 중";
            case PAYMENT_COMPLETED -> "결제 완료";
            case TRADE_COMPLETED -> "최종 거래 완료";
            case CANCELLED_BY_SYSTEM -> "시스템에 의해 거래 취소";
            default -> status.name();
        };
    }

    private AuctionStatus mapToAuctionStatus(SellingStatus sellingStatus) {
        return switch (sellingStatus) {
            case BEFORE, SALE, PROGRESS -> AuctionStatus.PROGRESS; // 진행 중
            case FINISH -> AuctionStatus.FINISHED; // SellingStatus의 FINISH를 AuctionStatus의 FINISHED로 매핑
            case COMPLETED -> AuctionStatus.TRADE_COMPLETED; // 거래 완료로 매핑
            default -> AuctionStatus.PROGRESS;
        };
    }
}
