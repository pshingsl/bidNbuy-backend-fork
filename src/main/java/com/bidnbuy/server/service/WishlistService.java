package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.WishlistDto;
import com.bidnbuy.server.dto.WishlistResponseDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.WishlistEntity;
import com.bidnbuy.server.enums.IsDeletedStatus;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.enums.WishlistFilterStatus;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.ImageRepository;
import com.bidnbuy.server.repository.UserRepository;
import com.bidnbuy.server.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final AuctionProductsRepository auctionProductsRepository;
    private final AuctionProductsService auctionProductsService;
    private final ImageRepository imageRepository;

    @Transactional
    public WishlistDto like(Long userId, Long auctionId) {

        // 유저 존재하는지 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 ID가 없습니다"));

        // 해당 경매 물품이 있는지 확인
        AuctionProductsEntity auction = auctionProductsRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("해당 경매 물품 ID가 없습니다"));

        // 판매자 유저 존재 여부 확인 후 본인 체크
        UserEntity seller = auction.getUser();

        // 판매자 유저가 있고(널 체크), 본인이 등록한 물품이면 찜 금지
        if(seller != null && seller.getUserId().longValue() == userId.longValue()) {
            throw new RuntimeException("자신이 등록한 경매 물품은 찜 할 수 없습니다.");
        }

        // 3. 찜 상태 확인 -> 로그인한 사용자가 경매 물품을 찜했는지 찾는다.
        return wishlistRepository.findByUserAndAuction(user, auction)
                .map(wishlist -> {
                    // 4. 찜이 존재하면 -> 찜 취소(DELETE)
                    wishlistRepository.delete(wishlist);

                    // 5. 삭제 후, 총 찜 개수와 상태를 반환
                    Integer wishCount = wishlistRepository.countByAuction(auction);
                    return WishlistDto.builder()
                            .isLiked(false) // 찜 취소됨
                            .wishCount(wishCount)
                            .auctionId(auctionId)
                            .build();
                })
                .orElseGet(() -> {
                    // 6. 찜이 존재하지 않으면 -> 찜 등록(CREATE)
                    WishlistEntity newWishlist = WishlistEntity.builder()
                            .user(user)
                            .auction(auction)
                            .createdAt(LocalDateTime.now())
                            .isDeleted(IsDeletedStatus.N)
                            .build();
                    wishlistRepository.save(newWishlist);

                    // 7. 등록 후, 총 찜 개수와 상태를 반환
                    Integer wishCount = wishlistRepository.countByAuction(auction);
                    return WishlistDto.builder()
                            .isLiked(true) // 찜 등록됨
                            .wishCount(wishCount)
                            .auctionId(auctionId)
                            .build();
                });
    }

    // 조회
    @Transactional(readOnly = true)
    public List<WishlistResponseDto> getWishlist(Long userId, WishlistFilterStatus filterStatus) {

        // 1. 유저 존재하는지 확인 (기존 로직 유지)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 ID가 없습니다"));

        // 2. 해당 경매 물품이 있는지 확인 (기존 로직 유지)
        List<WishlistEntity> list =  wishlistRepository.findByUserWithAuctionAndImages(user);

        return list.stream()
                .filter(wish -> {
                    AuctionProductsEntity product = wish.getAuction();

                    String currentStatusString = auctionProductsService.calculateSellingStatus(product);

                    if (filterStatus == WishlistFilterStatus.ALL){
                        return true;
                    }

                    // ✨ [핵심 수정] 1. 상태 문자열을 기반으로 그룹을 판단합니다.
                    // 이 부분이 기존의 Enum 변환/오류 로직을 대체합니다.

                    // 진행 중 그룹: "경매 진행 중" 또는 "경매 시작전"
                    boolean isProgressGroup = "진행중".equals(currentStatusString)
                            || "시작전".equals(currentStatusString);

                    // 종료 그룹: "경매 종료", "판매완료", "판매자 삭제완료"
                    boolean isFinishedGroup = "종료".equals(currentStatusString)
                            || "완료".equals(currentStatusString)
                            || "삭제".equals(currentStatusString); // ✨ 라벨 통일 및 삭제 상태 포함

                    // 2. 필터링 로직 (기존 의도 유지)

                    if (filterStatus == WishlistFilterStatus.FINISHED) {
                        return isFinishedGroup; // 종료 그룹 상태인 경우 포함
                    }

                    if (filterStatus == WishlistFilterStatus.PROGRESS) {
                        return isProgressGroup; // 진행 중 그룹 상태인 경우 포함
                    }

                    return false;
                })
                .map(wish -> {
                    AuctionProductsEntity product = wish.getAuction();

                    // AuctionProductsService에서 최종 라벨을 가져옵니다.
                    String sellingStatus = auctionProductsService.calculateSellingStatus(product);

                    String mainImageUrl = imageRepository.findFirstImageUrlByAuctionId(product.getAuctionId())
                            .orElse(null);

                    String sellerNicknameSafe = "탈퇴회원";
                    try {
                        if (product.getUser() != null) {
                            sellerNicknameSafe = product.getUser().getNickname();
                        }
                    } catch (Exception e) {
                        // keep fallback
                    }

                    return WishlistResponseDto.builder()
                            .auctionId(product.getAuctionId())
                            .title(product.getTitle())
                            .mainImageUrl(mainImageUrl)
                            .currentPrice(product.getCurrentPrice())
                            .endTime(product.getEndTime())
                            .sellerNickname(sellerNicknameSafe)
                            .sellingStatus(sellingStatus)
                            .build();
                })
                .collect(Collectors.toList());

        // 필터링
    }
}



