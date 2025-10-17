package com.bidnbuy.server.entity;

import com.bidnbuy.server.enums.InquiryEnums;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiries_id")
    private Long inquiriesId;

    // FK: 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // FK: 관리자 (문의 등록 시점에는 아직 지정 안될 수 있음 → nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = true)
    private AdminEntity admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(20) default 'GENERAL'")
    private InquiryEnums.InquiryType type; // GENERAL, REPORT

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'WAITING'")
    private InquiryEnums.InquiryStatus status; // WAITING, COMPLETE

    // 관리자 답변
    @Column(name = "request_title")
    private String requestTitle;

    @Column(name = "request_content", columnDefinition = "TEXT")
    private String requestContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;
}

