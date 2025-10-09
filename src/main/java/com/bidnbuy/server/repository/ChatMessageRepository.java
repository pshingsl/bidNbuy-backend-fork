package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.ChatMessageEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByChatroomIdOrderByCreateAt(ChatRoomEntity chatRoom);
}
