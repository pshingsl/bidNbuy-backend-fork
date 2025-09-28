package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.IsDeletedStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// 찜목록
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="WishList")
public class WishLisEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정
    @JoinColumn(name = "user_id", nullable = false) // FK 컬럼 이름 지정
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime created_at;

    @Column(name = "update_at", nullable = false)
    private LocalDateTime update_at;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false)
    private IsDeletedStatus is_deleted;

}
