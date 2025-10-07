package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ChatRoomCreateRequestDto;
import com.bidnbuy.server.dto.ChatRoomDto;
import com.bidnbuy.server.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/chatrooms")
@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    @PostMapping("/create")
    public ResponseEntity<ChatRoomDto> createChatRoom(@RequestBody ChatRoomCreateRequestDto requestDto,
                            @AuthenticationPrincipal Long userId){
        requestDto.setBuyerId(userId);
        try{
            ChatRoomDto chatRoom = chatRoomService.findOrCreateChatRoom(requestDto);
                    return ResponseEntity.ok(chatRoom);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }catch (Exception e){
            throw new RuntimeException("채팅방 생성 중 오류 발생",e );
        }
    }
}
