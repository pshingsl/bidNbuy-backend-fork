package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatMessageRequestDto;
import com.bidnbuy.server.dto.ResponseDto;
import com.bidnbuy.server.entity.UserEntity;
import com.bidnbuy.server.service.ChatMessageService;
import com.bidnbuy.server.service.ImageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messageSendingTemplate;
    private final ChatMessageService chatMessageService;
    private final ImageService imageService;

    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageRequestDto requestDto, SimpMessageHeaderAccessor accessor){
        Principal principal = accessor.getUser();

        if(principal == null){
            log.error("Principal is null. STOMP 세션에 인증 정보가 없어 메시지 전송을 거부합니다. (Principal: {})", principal);
            return;
        }

        Long senderId;
        try{
            senderId = Long.parseLong(principal.getName());
        }catch (Exception e){
            log.error("인증된 사용자 ID 추출 실패 (Long 캐스팅 오류 또는 Principal 구조 불일치): {}", e.getMessage());
            return;
        }
        log.info("메세지 수신 : Room={}, Sender={}, Content={}", requestDto.getChatroomId(), senderId, requestDto.getMessage());

        ChatMessageDto saveMessage = chatMessageService.saveAndProcessMessage(requestDto, senderId);

        String destination = "/topic/chat/room/"+requestDto.getChatroomId();
        messageSendingTemplate.convertAndSend(destination, saveMessage);

        log.info("메세지 브로드캐스트 : destination={}", destination);
    }

    @PutMapping("/chat/{chatroomId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long chatroomId, @AuthenticationPrincipal UserDetails userDetails){
        Long currentUserId = ((UserEntity) userDetails).getUserId();
        chatMessageService.processMarkingMessageAsRead(chatroomId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/chat/{chatroomId}/image")
    public ResponseEntity<String> uploadChatImage(@PathVariable Long chatroomId,
                                                  Principal principal,
                                                  @RequestParam("file")MultipartFile imageFile){
        if(principal == null){
            return ResponseEntity.status(401).body("인증되지 않은 사용자");
        }
        Long userId;
        try {
            userId = Long.parseLong(principal.getName());
        }catch (NumberFormatException e) {
            return ResponseEntity.status(403).body("올바르지 않은 사용자 id");
        }

        try{
            String imageUrl = imageService.uploadChatMessageImage(chatroomId, userId, imageFile);
            return ResponseEntity.ok(imageUrl);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage()); //빈 파일
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage()); //채팅방 , 사용자 문제
        } catch (RuntimeException e){
            return ResponseEntity.status(500).body("이미지 업로드 중 서버 오류 발생");
        }
    }
}
