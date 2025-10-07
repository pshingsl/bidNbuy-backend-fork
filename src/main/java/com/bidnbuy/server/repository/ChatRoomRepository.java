package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    Optional<ChatRoomEntity> findByBuyerIdAndSellerIdAndAuctionId(
            UserEntity buyerId,
            UserEntity sellerId,
            AuctionProductsEntity auctionId
    );
}
