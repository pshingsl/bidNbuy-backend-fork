package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.AuctionBidDto;
import com.bidnbuy.server.dto.BidUpdateDto;
import com.bidnbuy.server.entity.AuctionBidsEntity;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.repository.AuctionBidRepository;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuctionBidService {

    private final UserRepository userRepository;
    private final AuctionProductsRepository auctionProductsRepository;
    private final AuctionBidRepository auctionBidRepository;
    private final SimpMessagingTemplate messagingTemplate; // 웹소켓 메세지 전송을 위해 필요

    @Transactional
    public AuctionBidDto bid(Long userId, Long auctionId, Integer bidPrice) {

        // 1. 사용자 및 경매 물품 유효성 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND, 존재하지 않는 사용자입니다."));

        // 2. 경매 상태, 입찰금액 유효성 검증
        AuctionProductsEntity auctionProduct = auctionProductsRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> new RuntimeException("AUCTION_NOT_FOUND, 존재하지 않는 경매 물품입니다."));

        // 경매가 진행 중인지 확인
        if (auctionProduct.getSellingStatus() != SellingStatus.PROGRESS) {
            throw new RuntimeException("AUCTION_NOT_IN_PROGRESS, 현재 입찰이 불가능합니다. 경매가 진행 중이 아닙니다.");
        }

        // 사용자의 최소 입찰 금애이 현재 입찰 금액 비교
        Integer minBid = auctionProduct.getCurrentPrice() + auctionProduct.getMinBidPrice();
        if (bidPrice < minBid) {
            throw new RuntimeException("입찰 금액이 최소 입찰 단위(" + auctionProduct.getMinBidPrice() + "원)를 충족하지 못합니다. 최소 입찰 금액은 " + minBid + "원 이상입니다.");
        }

        // 3 DB에 저장
        AuctionBidsEntity newBid = AuctionBidsEntity.builder()
                .user(user)
                .auction(auctionProduct)
                .bidPrice(bidPrice)
                .build();

        auctionBidRepository.save(newBid);

        // Lock걸리 경매 물품의 최고가와 입찰 횟수 갱신
        auctionProduct.setCurrentPrice(bidPrice);
        auctionProduct.setBidCount(auctionProduct.getMinBidPrice() + 1);

        auctionProductsRepository.save(auctionProduct);

        // 웹소켓을 이용한 알림전송 디티오
        BidUpdateDto updateDto = BidUpdateDto.builder()
                .auctionId(auctionId)
                .currentPrice(auctionProduct.getCurrentPrice())
                .bidCount(auctionProduct.getBidCount())
                .lastBidderId(userId)
                .build();

        messagingTemplate.convertAndSend("/topic/auctions/" + auctionId, updateDto);

        return AuctionBidDto.builder()
                .bidId(newBid.getBidId())
                .userId(userId)
                .auctionId(auctionId)
                .bidPrice(bidPrice)
                .bidTime(newBid.getBidTime())
                .build();
    }
}
