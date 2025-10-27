package com.bidnbuy.server.service;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatMessageRequestDto;
import com.bidnbuy.server.dto.ChatReadStatusUpdateDto;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

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
                .messageType(requestDto.getMessageType())
                .build();

        ChatMessageEntity savedEntity =chatMessageRepository.save(chatMessageEntity);//db저장

        chatRoom.setLastMessagePreview(savedEntity.getMessage());
        chatRoom.setLastMessageTime(savedEntity.getCreateAt());

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

    //결제 완료시 자동 메세지 보내기
    @Transactional
    public void sendAutoMessage(Long chatroomId, String message ){
        UserEntity autoSender = userRepository.findByNickname("SYSTEM")
                .orElseThrow(()-> new EntityNotFoundException("시스템 유저 오류 발생, 발신자를 찾지 못해 발신 실패"));

        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(()->new EntityNotFoundException("채팅방을 찾는데 실패했습니다."));

        ChatMessageEntity autoMessageEntity = ChatMessageEntity.builder()
                .chatroomId(chatRoom)
                .senderId(autoSender)
                .message(message)
                .messageType(ChatMessageEntity.MessageType.SYSTEM) // 자동 메시지 타입
                .build();

        //디비 저장
        ChatMessageEntity savedEntity = chatMessageRepository.save(autoMessageEntity);

        chatRoom.setLastMessagePreview(savedEntity.getMessage());
        chatRoom.setLastMessageTime(savedEntity.getCreateAt());
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

    public ChatMessageDto convertToDto(ChatMessageEntity entity){
        return ChatMessageDto.builder()
                .chatmessageId(entity.getChatmessageId())
                .chatroomId(entity.getChatroomId().getChatroomId())
                .senderId(entity.getSenderId().getUserId())
                .message(entity.getMessage())
                .imageUrl(entity.getImageUrl())
                .messageType(entity.getMessageType())
                .createdAt(entity.getCreateAt())
                .isRead(entity.isRead())
                .build();
    }

    //메세지 읽음처리
    @Transactional
    public void processMarkingMessageAsRead(Long chatroomId, Long currentUserId){
        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(()-> new EntityNotFoundException("채팅방을 찾을 수 없음"));
        UserEntity reader = userRepository.findById(currentUserId)
                .orElseThrow(()-> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Long buyerId = chatRoom.getBuyerId().getUserId();
        Long sellerId = chatRoom.getSellerId().getUserId();
        if(!currentUserId.equals(buyerId)&& !currentUserId.equals(sellerId)){
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
        markMessagesAsRead(chatRoom, reader);
    }

    //메세지 읽음 처리하기(실시간 반영)
    @Transactional
    public void markMessagesAsRead(ChatRoomEntity chatRoom, UserEntity reader){
        try{
            int updatedCount = chatMessageRepository.markMessagesAsRead(chatRoom, reader);
            if(updatedCount > 0){
                String destination = "/topic/chat/readstatus/"+chatRoom.getChatroomId();
                ChatReadStatusUpdateDto updateDto = new ChatReadStatusUpdateDto(
                        chatRoom.getChatroomId(),
                        reader.getUserId(),
                        updatedCount
                );

                messagingTemplate.convertAndSend(destination, updateDto);
                log.info("웹소켓 활용- 채팅방 {}의 읽음 상태 업데이트", chatRoom.getChatroomId());
            }
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
