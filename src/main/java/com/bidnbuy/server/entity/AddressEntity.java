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
}
