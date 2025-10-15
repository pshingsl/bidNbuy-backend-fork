package com.bidnbuy.server.repository;

import com.bidnbuy.server.entity.ChatMessageEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByChatroomIdOrderByCreateAt(ChatRoomEntity chatRoom);

    Long countByChatroomIdAndSenderIdNotAndIsRead(ChatRoomEntity chatRoom, UserEntity sender, boolean isRead);

    @Modifying
    @Query("UPDATE ChatMessageEntity c SET c.isRead = true " +
            "WHERE c.chatroomId = :chatRoom AND c.senderId != :reader AND c.isRead = false")
    int markMessagesAsRead(@Param("chatRoom") ChatRoomEntity chatRoom, @Param("reader") UserEntity reader);

}
