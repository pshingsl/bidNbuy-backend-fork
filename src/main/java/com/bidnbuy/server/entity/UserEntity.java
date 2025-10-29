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
@Table(name = "User", uniqueConstraints = {
        @UniqueConstraint(name = "UC_EmailAndDeletedAt", columnNames = {"email", "deleted_at"}),
})
@SQLDelete(sql = "UPDATE user SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = true) //나중에 false로 바꾸기
    private AdminEntity admin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> address = new ArrayList<>();

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuthStatus authStatus = AuthStatus.N;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus userStatus = UserStatus.Y;

    @Column(name = "user_type", length = 20)
    private String userType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
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
    @Builder.Default
    @Column(name = "user_temperature", nullable = false, columnDefinition = "double default 0.0")
    private Double userTemperature = 0.0;

    // 페널티 (누적 점수)
    @Column(name = "penalty_points", nullable = false)
    @Builder.Default
    private int penaltyPoints = 0;

    // 정지 해제 예정일
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    // 정지 상태
    @Column(name = "is_suspended", nullable = false)
    @Builder.Default
    private boolean isSuspended = false;

    // 정지 횟수 (최초 1회 제한)
    @Column(name = "suspension_count", nullable = false)
    @Builder.Default
    private int suspensionCount = 0;

    // 강퇴 횟수 (최초 1회 제한)
    @Column(name = "ban_count", nullable = false)
    @Builder.Default
    private int banCount = 0;

    // 계좌 관련
    @Column(name = "bank_name", length = 50)
    private String bankName;   // 은행명

    @Column(name = "account_number", length = 50)
    private String accountNumber;   // 계좌번호

    @Column(name = "account_holder", length = 50)
    private String accountHolder;   // 예금주
}