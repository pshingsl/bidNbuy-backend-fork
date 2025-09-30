package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.IsVerified;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

// 이미지 엔티티
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="Image")
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = true)
    private AuctionProductsEntity auctionProduct ;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private UserEntity user;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_type", nullable = false)
    private String imageType;
}