package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.InquiriesEntity;
import com.bidnbuy.server.enums.InquiryEnums;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // 관리자용 추가 - 최신순
    // 전체 조회
    Page<InquiriesEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 타입별
    Page<InquiriesEntity> findByTypeOrderByCreatedAtDesc(InquiryEnums.InquiryType type, Pageable pageable);
    
    // 상태별
    Page<InquiriesEntity> findByStatusOrderByCreatedAtDesc(InquiryEnums.InquiryStatus status, Pageable pageable);
    
    // 타입과 상태별
    Page<InquiriesEntity> findByTypeAndStatusOrderByCreatedAtDesc(InquiryEnums.InquiryType type, InquiryEnums.InquiryStatus status, Pageable pageable);
    
    // 제목으로 검색
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);
    
    // 작성자(이메일)로 검색
    @Query("SELECT i FROM InquiriesEntity i JOIN i.user u WHERE u.email LIKE %:email% ORDER BY i.createdAt DESC")
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(@Param("email") String email, Pageable pageable);
    
    // 제목+타입
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(String title, InquiryEnums.InquiryType type, Pageable pageable);
    
    // 제목+상태
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(String title, InquiryEnums.InquiryStatus status, Pageable pageable);
    
    // 제목+타입+상태로 검색
    Page<InquiriesEntity> findByTitleContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(String title, InquiryEnums.InquiryType type, InquiryEnums.InquiryStatus status, Pageable pageable);
    
    // 작성자+타입
    @Query("SELECT i FROM InquiriesEntity i JOIN i.user u WHERE u.email LIKE %:email% AND i.type = :type ORDER BY i.createdAt DESC")
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseAndTypeOrderByCreatedAtDesc(@Param("email") String email, @Param("type") InquiryEnums.InquiryType type, Pageable pageable);
    
    // 작성자+상태
    @Query("SELECT i FROM InquiriesEntity i JOIN i.user u WHERE u.email LIKE %:email% AND i.status = :status ORDER BY i.createdAt DESC")
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseAndStatusOrderByCreatedAtDesc(@Param("email") String email, @Param("status") InquiryEnums.InquiryStatus status, Pageable pageable);
    
    // 작성자+타입+상태
    @Query("SELECT i FROM InquiriesEntity i JOIN i.user u WHERE u.email LIKE %:email% AND i.type = :type AND i.status = :status ORDER BY i.createdAt DESC")
    Page<InquiriesEntity> findByUserEmailContainingIgnoreCaseAndTypeAndStatusOrderByCreatedAtDesc(@Param("email") String email, @Param("type") InquiryEnums.InquiryType type, @Param("status") InquiryEnums.InquiryStatus status, Pageable pageable);
}
