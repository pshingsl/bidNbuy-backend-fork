package com.bidnbuy.server.entity;

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
@Table(name="Address")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="address_id", nullable = true) //구현시 notnull로 바꿔서 사용
    private long addressId;

    // 유저 실제 이름 -> 유저에서 처리하면 데이터 무결성이 발생해서 여기에다 컬럼 추가
    @Column(name="recipient_name", nullable = false, length = 50)
    private String recipientName;

    // 유저 전화번호
    @Column(name="phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name="zonecode", nullable = false, length = 10)
    private String zonecode;

    @Column(name="address", nullable = false, length = 255)
    private String address;

    @Column(name="address_type", nullable = false) //char따로 설정 필요하면 추가 필요
    private char addressType;

    @Column(name="detail_address", nullable = true, length = 255)
    private String detailAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}
