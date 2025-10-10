package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.entity.WishlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    // 사용자가 특정 경매 물품을 찜 했는지 확인
    // select * from wish_list where user = 1 and auction=1
    Optional<WishlistEntity> findByUserAndAuction(UserEntity user, AuctionProductsEntity auction);

    // select count(auction) from wishlist w where w.auction
    Integer countByAuction(AuctionProductsEntity auction);

    // select from wishlist w where useer.w
    List<WishlistEntity> findByUser(UserEntity user);
}