package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.IsDeletedStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

// 찜목록
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="WishList", uniqueConstraints = {
        @UniqueConstraint(name = "USER_AUCTION_WISH", columnNames = {"user_id", "auction_id"})
})
public class WishlistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼 이름 지정
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정
    @JoinColumn(name = "auction_id", nullable = false) // FK 컬럼 이름 지정
    private AuctionProductsEntity auction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at", nullable = true)
    private LocalDateTime updateAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = true)
    private IsDeletedStatus isDeleted;

}