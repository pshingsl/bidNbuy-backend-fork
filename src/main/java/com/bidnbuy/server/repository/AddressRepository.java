package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity,Long> {

    List<AddressEntity> findByUser_UserId(Long userId);

    // 해당 유저의 가장 최근 주소 1개
    Optional<AddressEntity> findFirstByUser_UserIdOrderByCreatedAtDesc(Long userId);
}
