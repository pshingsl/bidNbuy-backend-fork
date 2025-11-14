package com.bidnbuy.server.config;

import com.bidnbuy.server.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        //stomp 연결 명령
        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            log.info("[STOMP] CONNECT 요청 수신");

            //토큰 추출
            String authorizationHeader = accessor.getFirstNativeHeader(("authorization"));
            log.info("[STOMP] Authorization 헤더: {}", authorizationHeader);

            if(authorizationHeader !=null && authorizationHeader.startsWith("Bearer")){
//                String token = (String) accessor.getFirstNativeHeader("Auth-Token");
                String token = authorizationHeader.substring(7).trim();
                //토큰 유효성 검증 및 userID추출
                if(jwtProvider.validateToken(token)){
                    try{
                        final Long userIdLong = jwtProvider.getUserIdFromToken(token);
                        if (userIdLong != null) {
                            log.info("[STOMP] 인증 성공 - userId: {}", userIdLong);
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userIdLong,
                                null,
                                Collections.emptyList()
//                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        accessor.setUser(authentication);
                        } else {
                            log.warn("[STOMP] 토큰 검증은 성공했으나 사용자 ID 추출 실패");
                            throw new MessageDeliveryException("토큰 검증 후 ID 추출 실패 (ID null).");
                        }
                    } catch (Exception e) {
                        log.error("[STOMP] JWT 처리 중 예외 발생", e);
                        throw new MessageDeliveryException("JWT 처리 중 치명적인 오류 발생 (서버 로그 확인): " + e.getMessage());
                    }
                }else{
                    log.warn("[STOMP] 토큰 유효성 검사 실패");
                    throw new MessageDeliveryException("invalid jwt token");
                }
            }else{
                log.warn("[STOMP] Authorization 헤더 없음 또는 형식 오류");
                throw new MessageDeliveryException("토큰 없음, 인증되지 않은 연결이라 거부합니다~");
            }
        }
        return message;
    }
}
