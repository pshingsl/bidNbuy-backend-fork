package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatMessageRequestDto;
import com.bidnbuy.server.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messageSendingTemplate;
    private final ChatMessageService chatMessageService;

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
}
