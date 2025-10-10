package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatMessageRequestDto;
import com.bidnbuy.server.entity.ChatMessageEntity;
import com.bidnbuy.server.entity.ChatRoomEntity;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.repository.ChatMessageRepository;
import com.bidnbuy.server.repository.ChatRoomRepository;
import com.bidnbuy.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    //db저장 후 dto반환
    @Transactional
    public ChatMessageDto saveAndProcessMessage(ChatMessageRequestDto requestDto, Long senderId){
        Long generatedMessageId = 100L;

        //엔티티 조회해서 객체 찾아오기
        ChatRoomEntity chatRoom = chatRoomRepository.findById(requestDto.getChatroomId())
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + requestDto.getChatroomId()));

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(()-> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        ChatMessageEntity chatMessageEntity  = ChatMessageEntity.builder()
                .chatroomId(chatRoom)
                .senderId(sender)
                .message(requestDto.getMessage())
                .messageType(ChatMessageEntity.MessageType.CHAT)
                .build();

        ChatMessageEntity savedEntity =chatMessageRepository.save(chatMessageEntity);//db저장


        return ChatMessageDto.builder()
                .chatmessageId(savedEntity.getChatmessageId())
                .chatroomId(savedEntity.getChatroomId().getChatroomId())
                .senderId(savedEntity.getSenderId().getUserId())
                .message(savedEntity.getMessage())
                .imageUrl(savedEntity.getImageUrl())
                .messageType(savedEntity.getMessageType())
                .createdAt(savedEntity.getCreateAt())
                .isRead(savedEntity.isRead())
                .build();
    }
}
