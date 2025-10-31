package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatRoomCreateRequestDto;
import com.bidnbuy.server.dto.ChatRoomDto;
import com.bidnbuy.server.dto.ChatRoomListDto;
import com.bidnbuy.server.security.CustomUserDetailsService;
import com.bidnbuy.server.service.ChatMessageService;
import com.bidnbuy.server.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "체팅 방 관련 API", description = "채팅 방 관련 기능 제공")
@Slf4j
@RequestMapping("/chatrooms")
@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(
        summary = "채팅방 생성",
        description = "경매상품, 유저 아이디 기준으로 채팅방 생성",
        tags={"체팅 방 관련 API"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "채팅방 생성 완료",
            content=@Content(schema = @Schema(implementation = Void.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 정보 없음(Principal null)",
            content = @Content(schema = @Schema(example = "인증되지않은 사용자"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "필수 엔티티 (경매상품)을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "채팅방 생성 중 서버 오류 발생"
        )
    })
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

    @Operation(
        summary ="채팅방의 메시지 목록 조회",
        description = "특정 채팅방 메시지 목록 조회",
        tags={"체팅 방 관련 API"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "메시지 목록 조회",
            content = @Content(schema = @Schema(implementation = ChatMessageDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 정보 없음",
            content = @Content(schema = @Schema(type = "string", example = "인증되지 않은 사용자"))
        )
    })
    //메세지 조회 + 읽음 처리
    @GetMapping("/{chatroomId}/message")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(
            @PathVariable("chatroomId") Long chatroomId,
            @AuthenticationPrincipal Long currentUserId){

        log.info("채팅방 메시지 조회 요청: chatroomId={}, userId={}", chatroomId, currentUserId);

        List<ChatMessageDto> messages = chatMessageService.getMessageByChatRoomId(chatroomId, currentUserId);

        return ResponseEntity.ok(messages);
    }

    @Operation(
        summary = "참여 중인 채팅방 목록 조회",
        description = "사용자가 참여한, 참여된 모든 채팅방 목록을 최신 메시지와 함께 조회",
        tags={"체팅 방 관련 API"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "채팅방 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ChatRoomListDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "사용자 id 추출 실패",
            content = @Content(schema = @Schema(type = "string", example = "사용자 인증에 필요한 정보 부족"))
        )
    })
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

        chatRoomService.deletedChatRoom(chatroomId, currnetUserId);

        return ResponseEntity.noContent().build();
    }
}
