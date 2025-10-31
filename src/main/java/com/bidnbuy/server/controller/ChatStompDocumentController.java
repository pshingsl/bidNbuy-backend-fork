package com.bidnbuy.server.controller;

import com.bidnbuy.server.dto.ChatMessageDto;
import com.bidnbuy.server.dto.ChatMessageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

//swagger문서를 위한 더미 컨트롤러 파일입니다. (문서화 목적으로 작성핳ㅁ. 실제 로직 X)
@RestController
@Tag(name="Web Socket기반 채팅", description = "STOMP기반 채팅 메세지 송수신 API")
public class ChatStompDocumentController {
    @Operation(
        summary = "채팅 메시지 전송(STOMP SEND)",
        description = " 클라이언트 요청 (SEND) : `/chat/message",
            tags = {"Web Socket기반 채팅"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "서버가 구독 후 메세지 전송 (HTTP 응답 아님)",
                    content = @Content(schema = @Schema(implementation = ChatMessageDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패 (STOMP 세션에 Principal 없음)")
    })
    @PostMapping(value = "/stomp-docs/chat/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void documentSendMessage(
            @RequestBody(
                    description="STOMP SEND프레임 전송 DTO",
                    required = true,
                    content=@Content(schema = @Schema(implementation = ChatMessageDto.class))
            )
        ChatMessageRequestDto requestDto){
        //살제 실행되지 않기 위해 공백
    }
}
