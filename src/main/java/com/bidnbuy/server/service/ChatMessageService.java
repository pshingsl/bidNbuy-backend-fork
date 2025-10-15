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
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    //db저장 후 dto반환
    @Transactional
    public ChatMessageDto saveAndProcessMessage(ChatMessageRequestDto requestDto, Long senderId){
//        Long generatedMessageId = 100L;

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

        chatRoom.setLastMessagePreview(savedEntity.getMessage());
        chatRoom.setLastMessageTime(savedEntity.getCreateAt());

        return ChatMessageDto.builder()
                .chatmessageId(String.valueOf(savedEntity.getChatmessageId()))
                .chatroomId(String.valueOf(savedEntity.getChatroomId().getChatroomId()))
                .senderId(String.valueOf(savedEntity.getSenderId().getUserId()))
                .message(savedEntity.getMessage())
                .imageUrl(savedEntity.getImageUrl())
                .messageType(savedEntity.getMessageType())
                .createdAt(savedEntity.getCreateAt())
                .isRead(savedEntity.isRead())
                .build();
    }

    //채팅 메세지 조회
    @Transactional
    public List<ChatMessageDto> getMessageByChatRoomId(Long chatroomId, Long currentUserId){
        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(()-> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));
        UserEntity currentUser = userRepository.findById(currentUserId)
                .orElseThrow(()-> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Long buyerId = chatRoom.getBuyerId().getUserId();
        Long sellerId = chatRoom.getSellerId().getUserId();

        if(currentUserId.equals(buyerId)||currentUserId.equals(sellerId)){
            markMessagesAsRead(chatRoom, currentUser);
        }else{
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        List<ChatMessageEntity> messages = chatMessageRepository.findByChatroomIdOrderByCreateAt(chatRoom);

        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDto convertToDto(ChatMessageEntity entity){
        return ChatMessageDto.builder()
                .chatmessageId(String.valueOf(entity.getChatmessageId()))
                .chatroomId(String.valueOf(entity.getChatroomId().getChatroomId()))
                .senderId(String.valueOf(entity.getSenderId().getUserId()))
                .message(entity.getMessage())
                .imageUrl(entity.getImageUrl())
                .messageType(entity.getMessageType())
                .createdAt(entity.getCreateAt())
                .isRead(entity.isRead())
                .build();
    }

    //메세지 읽음 처리하기
    @Transactional
    public void markMessagesAsRead(ChatRoomEntity chatRoom, UserEntity reader){
        try{
            int updatedCount = chatMessageRepository.markMessagesAsRead(chatRoom, reader);
            log.info("채팅방{}에서 사용자 {}가 {}개의 메시지를 읽음", chatRoom.getChatroomId(), reader.getUserId(), updatedCount);
        }catch (Exception e){
            log.error("메세지 읽음 처리 중 오류 발생", e);
        }
    }

    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(Long chatroomId, Long currentUserId){
        //채팅방 조회
        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        UserEntity currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        //권한 확인
        Long buyerId = chatRoom.getBuyerId().getUserId();
        Long sellerId = chatRoom.getSellerId().getUserId();
        if(!currentUserId.equals(buyerId)&& !currentUserId.equals(sellerId)){
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
        return chatMessageRepository.countByChatroomIdAndSenderIdNotAndIsRead(
                chatRoom,
                currentUser,
                false
        );
    }




}
