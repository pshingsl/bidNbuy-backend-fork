package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquiriesRepository extends JpaRepository<InquiriesEntity, Long> {

    // 일반 조회(전체, 상태필터링) (문의)
    //List<InquiriesEntity> findByUserUserIdAndType(Long userId, InquiryEnums.InquiryType type);

    // 일반 조회 (전체)
    List<InquiriesEntity> findByUserUserId(Long userId);

    // 상세 조회 (문의)
    //Optional<InquiriesEntity> findByInquiriesIdAndUserUserIdAndType(Long inquiryId, Long userId, InquiryEnums.InquiryType type);

    // 상세 조회(전체)
    Optional<InquiriesEntity> findByInquiriesIdAndUserUserId(Long inquiryId, Long userId);

    // 일반 조회(신고)
    List<InquiriesEntity> findByUser_UserIdAndType(Long userId, InquiryEnums.InquiryType type);

    // 상세 조회(신고)
    Optional<InquiriesEntity> findByInquiriesIdAndType(Long inquiriesId, InquiryEnums.InquiryType type);

    // 관리자용 추가 - 최신순, 삭제 유저 포함
    // 전체 조회
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id",
           nativeQuery = true)
    Page<InquiriesEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 타입별
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.type = :type ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.type = :type",
           nativeQuery = true)
    Page<InquiriesEntity> findByTypeOrderByCreatedAtDesc(@Param("type") String type, Pageable pageable);
    
    // 상태별
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.status = :status ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.status = :status",
           nativeQuery = true)
    Page<InquiriesEntity> findByStatusOrderByCreatedAtDesc(@Param("status") String status, Pageable pageable);
    
    // 타입과 상태별
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.type = :type AND i.status = :status ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.type = :type AND i.status = :status",
           nativeQuery = true)
    Page<InquiriesEntity> findByTypeAndStatusOrderByCreatedAtDesc(@Param("type") String type, @Param("status") String status, Pageable pageable);
    
    // 제목으로 검색
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%')",
           nativeQuery = true)
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(@Param("title") String title, Pageable pageable);
    
    // 작성자(이메일)로 검색
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') ORDER BY i.created_at DESC, i.inquiries_id DESC", 
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%')",
           nativeQuery = true)
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(@Param("email") String email, Pageable pageable);
    
    // 제목+타입
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') AND i.type = :type ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') AND i.type = :type",
           nativeQuery = true)
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(@Param("title") String title, @Param("type") String type, Pageable pageable);
    
    // 제목+상태
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') AND i.status = :status ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') AND i.status = :status",
           nativeQuery = true)
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(@Param("title") String title, @Param("status") String status, Pageable pageable);
    
    // 제목+타입+상태로 검색
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') AND i.type = :type AND i.status = :status ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.title LIKE CONCAT('%', :title, '%') AND i.type = :type AND i.status = :status",
           nativeQuery = true)
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(@Param("title") String title, @Param("type") String type, @Param("status") String status, Pageable pageable);
    
    // 작성자+타입
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') AND i.type = :type ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') AND i.type = :type",
           nativeQuery = true)
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(@Param("email") String email, @Param("type") String type, Pageable pageable);
    
    // 작성자+상태
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') AND i.status = :status ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') AND i.status = :status",
           nativeQuery = true)
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(@Param("email") String email, @Param("status") String status, Pageable pageable);
    
    // 작성자+타입+상태
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') AND i.type = :type AND i.status = :status ORDER BY i.created_at DESC, i.inquiries_id DESC",
           countQuery = "SELECT COUNT(*) FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE u.email LIKE CONCAT('%', :email, '%') AND i.type = :type AND i.status = :status",
           nativeQuery = true)
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(@Param("email") String email, @Param("type") String type, @Param("status") String status, Pageable pageable);

    // 문의 유저아이디 조회
    @Query(value = "SELECT user_id FROM inquiries WHERE inquiries_id = :inquiryId", nativeQuery = true)
    Long findUserIdByInquiryIdNative(@Param("inquiryId") Long inquiryId);

    // 문의 관리자아이디 조회
    @Query(value = "SELECT admin_id FROM inquiries WHERE inquiries_id = :inquiryId", nativeQuery = true)
    Long findAdminIdByInquiryIdNative(@Param("inquiryId") Long inquiryId);

    // 문의 상세
    @Query(value = "SELECT i.* FROM inquiries i LEFT JOIN `user` u ON u.user_id = i.user_id WHERE i.inquiries_id = :inquiryId",
           nativeQuery = true)
    Optional<InquiriesEntity> findByIdIncludingDeletedUser(@Param("inquiryId") Long inquiryId);

    // 문의 상태만 부분 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE InquiriesEntity i SET i.status = :status, i.updateAt = :now WHERE i.inquiriesId = :id")
    int updateStatusOnly(@Param("id") Long id, @Param("status") InquiryEnums.InquiryStatus status, @Param("now") java.time.LocalDateTime now);
}
