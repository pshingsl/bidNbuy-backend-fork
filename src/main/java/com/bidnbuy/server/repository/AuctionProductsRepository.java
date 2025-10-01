package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AuctionProductsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionProductsRepository extends JpaRepository<AuctionProductsEntity, Long> {

    @Query("SELECT p FROM AuctionProductsEntity p " +
    "JOIN FETCH p.user u " +
    "JOIN FETCH p.category c " +
    "WHERE p.startTime <= CURRENT_TIMESTAMP AND p.endTime > CURRENT_TIMESTAMP " +
    "ORDER BY p.endTime ASC"
    )
    Page<AuctionProductsEntity> findRunningAuctionsWithDetails(Pageable pageable);

    @Query("SELECT p FROM AuctionProductsEntity p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.category c " +
            "WHERE p.auctionId = :auctionId")
    Optional<AuctionProductsEntity> findByIdWithDetails(Long auctionId);
}