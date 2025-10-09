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
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessageSendingOperations messageSendingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageRequestDto requestDto, SimpMessageHeaderAccessor accessor){
        String senderIdStr = accessor.getUser().getName();
        Long senderId = Long.valueOf(senderIdStr);

        log.info("메세지 수신 : Room={}, Sender={}, Content={}", requestDto.getChatroomId(), senderId, requestDto.getMessage());

        ChatMessageDto saveMessage = chatMessageService.saveAndProcessMessage(requestDto, senderId);

        String destination = "chat/room"+requestDto.getChatroomId();
        messageSendingTemplate.convertAndSend(destination, saveMessage);

        log.info("메세지 브로드캐스트 : destination={}", destination);
    }
}
