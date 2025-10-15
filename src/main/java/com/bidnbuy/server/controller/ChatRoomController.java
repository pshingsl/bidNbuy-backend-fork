package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatRoomCreateRequestDto;
import com.bidnbuy.server.dto.ChatRoomDto;
import com.bidnbuy.server.dto.ChatRoomListDto;
import com.bidnbuy.server.security.CustomUserDetailsService;
import com.bidnbuy.server.service.ChatMessageService;
import com.bidnbuy.server.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/chatrooms")
@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/{auctionId}")
    public ResponseEntity<ChatRoomDto> createChatRoom(@PathVariable Long auctionId,
                                                      @AuthenticationPrincipal Long userId,
                                                      @RequestBody ChatRoomCreateRequestDto requestDto){
        requestDto.setAuctionId(auctionId);
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

    //메세지 조회 + 읽음 처리
    @GetMapping("/{chatroomId}/message")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(
            @PathVariable("chatroomId") Long chatroomId,
            @AuthenticationPrincipal Long currentUserId){
//        if (authentication == null) {
//            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
//        }
//        Object principal = authentication.getPrincipal();
//        Long currentUserId;
//
//        if (principal instanceof Long) {
//            currentUserId = (Long) principal;
//        } else {
//            log.error("인증 정보 Principal 타입 오류: {}", principal.getClass().getName());
//            throw new AccessDeniedException("인증 정보가 올바르지 않습니다. (Principal 타입 오류)");
//        }
        log.info("채팅방 메시지 조회 요청: chatroomId={}, userId={}", chatroomId, currentUserId);

        List<ChatMessageDto> messages = chatMessageService.getMessageByChatRoomId(chatroomId, currentUserId);

        return ResponseEntity.ok(messages);
    }

    //채팅방 리스트 가져오기
    @GetMapping("/list")
    public ResponseEntity<List<ChatRoomListDto>> getChatList(@AuthenticationPrincipal Long userId){
        if (userId == null) {
            throw new AccessDeniedException("인증 정보 없음");
        }
        List<ChatRoomListDto> chatList = chatRoomService.getChatRoomList(userId);
        return ResponseEntity.ok(chatList);
    }


    @GetMapping("/{chatroomId}/unreadcount")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long chatroomId,
            @AuthenticationPrincipal Long currentUserId){
        log.info("안 읽은 메시지 수 조회 : chatroomId={}, userId={}", chatroomId, currentUserId);
        Long count = chatMessageService.getUnreadMessageCount(chatroomId, currentUserId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{chatroomId}")
    public ResponseEntity<Void> deleteChatRoom(
            @PathVariable Long chatroomId,
            @AuthenticationPrincipal Long currnetUserId){
        log.info("채팅방 삭제: chatroomId={}, userId={}", chatroomId, currnetUserId);

        chatRoomService.deltedChatRoom(chatroomId, currnetUserId);

        return ResponseEntity.noContent().build();
    }
}
