package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.AuthStatus;
import com.bidnbuy.server.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="User")
@SQLDelete(sql = "UPDATE user SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long userId;

    @ManyToOne
    @JoinColumn(name="admin_id", nullable = true) //나중에 false로 바꾸기
    private AdminEntity adminId;

    @ManyToOne
    @JoinColumn(name="address_id", nullable = true)
    private AddressEntity addressId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private  String password;

    @Column(name = "nickname", nullable = false, length = 20)
    private  String nickname;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuthStatus authStatus = AuthStatus.N;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus userStatus = UserStatus.Y;

    @Column(name = "user_type", length = 20)
    private  String userType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private  LocalDateTime updatedAt;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshTokenEntity refreshToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionProductsEntity> auctionProducts = new ArrayList<>();

    private String role;

    //임시비번 해시값
    @Column(name = "temp_password_hash", length = 100)
    private String tempPasswordHash;

    //임시비번 만료시간
    @Column(name = "temp_password_expiry_date")
    private LocalDateTime tempPasswordExpiryDate;

    // 유저 프로필 이미지
    @Column(name = "profile_image_url", length = 512, nullable = true)
    private String profileImageUrl;

    // 유저 온도 (별점 평균 × 10)
    @Column(name = "user_temperature", nullable = true)
    private Double userTemperature;
}
