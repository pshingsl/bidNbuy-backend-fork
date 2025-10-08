package com.bidnbuy.server.config;

import com.bidnbuy.server.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        //stomp 연결 명령
        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            //토큰 추출
            String authorizationHeader = accessor.getFirstNativeHeader(("Authorization"));

            if(authorizationHeader !=null && authorizationHeader.startsWith("Bearer")){
                String token = authorizationHeader.substring(7).trim();
                //토큰 유효성 검증 및 userID추출
                if(jwtProvider.validateToken(token)){
                    final Long userId = Long.valueOf(jwtProvider.getUserIdFromToken(token));
                    //객체로 만들어 저장
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return String.valueOf(userId);
                        }
                    });
                }else{
                    throw new MessageDeliveryException("invalid jwt token");
                }
            }else{
                throw new MessageDeliveryException("토큰 없음, 인증되지 않은 연결이라 거부합니다~");
            }
        }
        return message;
    }
}
