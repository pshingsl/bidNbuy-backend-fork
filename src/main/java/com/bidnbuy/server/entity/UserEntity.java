package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.AuthStatus;
import com.bidnbuy.server.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="User")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long userId;

    @ManyToOne
    @JoinColumn(name="admin_id")
    private AdminEntity adminId;

    @ManyToOne
    @JoinColumn(name="address_id")
    private AddressEntity addressId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private  String password;

    @Column(name = "nickname", nullable = false, length = 20)
    private  String nickname;

    @Enumerated(EnumType.STRING)
    private AuthStatus authStatus;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column(name = "user_type", length = 20)
    private  String userType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = false)
    private  LocalDateTime updateAt;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;


}
