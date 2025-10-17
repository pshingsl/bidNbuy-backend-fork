package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.WishlistDto;
import com.bidnbuy.server.dto.WishlistResponseDto;
import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ImageEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.WishlistEntity;
import com.bidnbuy.server.enums.ImageType;
import com.bidnbuy.server.enums.IsDeletedStatus;
import com.bidnbuy.server.enums.SellingStatus;
import com.bidnbuy.server.enums.WishlistFilterStatus;
import com.bidnbuy.server.repository.AuctionProductsRepository;
import com.bidnbuy.server.repository.UserRepository;
import com.bidnbuy.server.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Transactional
    public WishlistDto like(Long userId, Long auctionId) {

        // 1. 유저 존재하는지 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 ID가 없습니다"));

        // 2. 해당 경매 물품이 있는지 확인
        AuctionProductsEntity auction = auctionProductsRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("해당 경매 물품 ID가 없습니다"));

        // 자기 자신이 등록한 물 찜하기 금지
        if(auction.getUser().getUserId() == userId.longValue()) {
            throw new RuntimeException("자신이 등록한 경매 물품은 찜 할 수 없습니다.");
        }

        // 3. 찜 상태 확인 -> 로그인한 사용자가 경매 물품을 찜했는지 찾는다.
        return wishlistRepository.findByUserAndAuction(user, auction)
                .map(wishlist -> {
                    // 4. 찜이 존재하면 -> 찜 취소(DELETE)
                    wishlistRepository.delete(wishlist);

                    // 5. 삭제 후, 총 찜 개수와 상태를 반환
                   Integer likeCount = wishlistRepository.countByAuction(auction);
                    return WishlistDto.builder()
                            .isLiked(false) // 찜 취소됨
                            .likeCount(likeCount)
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
                    Integer likeCount = wishlistRepository.countByAuction(auction);
                    return WishlistDto.builder()
                            .isLiked(true) // 찜 등록됨
                            .likeCount(likeCount)
                            .auctionId(auctionId)
                            .build();
                });
    }

    // 조회
    public List<WishlistResponseDto> getWishlist(Long userId, WishlistFilterStatus filterStatus) {

        // 1. 유저 존재하는지 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 ID가 없습니다"));

        // 2. 해당 경매 물품이 있는지 확인
        List<WishlistEntity> list =  wishlistRepository.findByUser(user);

        return list.stream()
                .filter(wish -> {
                    AuctionProductsEntity product = wish.getAuction();

                    String currentStatusString = auctionProductsService.calculateSellingStatus(product);

                    if (filterStatus == WishlistFilterStatus.ALL){
                        return true;
                    }

                    SellingStatus currentStatus;

                    try{
                        currentStatus = SellingStatus.valueOf(currentStatusString);
                    }catch (IllegalArgumentException e) {
                        return false;
                    }

                    boolean isFinished = currentStatus == SellingStatus.FINISH
                            || currentStatus == SellingStatus.COMPLETED;

                    if (filterStatus == WishlistFilterStatus.FINISHED) {
                        return isFinished;
                    }

                    if (filterStatus == WishlistFilterStatus.PROGRESS) {
                        // 진행 중 상태 그룹핑: 종료 상태가 아닌 모든 상태 (BEFORE, SALE, PROGRESS 등)
                        return !isFinished;
                    }

                    return false;
                })
                .map(wish -> {
                    AuctionProductsEntity product = wish.getAuction();

                    String mainImageUrl = product.getImages().stream()
                            .filter(image -> ImageType.PRODUCT.equals(image.getImageType())) // ImageEntity에 getImageType()이 있다고 가정
                            .findFirst()
                            .map(ImageEntity::getImageUrl) // ImageEntity에 getImageUrl()이 있다고 가정
                            .orElse(null);

                    String sellingStatus = auctionProductsService.calculateSellingStatus(product);

                    return WishlistResponseDto.builder()
                            .auctionId(product.getAuctionId())
                            .title(product.getTitle())
                            .mainImageUrl(mainImageUrl) // 이미지 리스트 중 메인 이미지를 가져온다고 가정
                            .currentPrice(product.getCurrentPrice())
                            .endTime(product.getEndTime())
                            .sellerNickname(product.getUser().getNickname()) // Fetch Join으로 User가 로드되어야 함
                            .sellingStatus(sellingStatus)
                            .build();
                })
                .collect(Collectors.toList());

        // 필터링
    }
}