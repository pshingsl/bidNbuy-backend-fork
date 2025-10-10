package com.bidnbuy.server.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor !=null && StompCommand.CONNECT.equals(accessor.getCommand())){
            String authToken = accessor.getFirstNativeHeader("Auth-Token");

            if(authToken != null && jwtProvider.validateToken(authToken)){
                Claims claims = jwtProvider.getClaims(authToken);
                String username = claims.getSubject();

                User user = new User(username, "", Collections.emptyList());
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    accessor.setUser(authenticationToken);

                log.info("STOMP CONNECT 인증 성공: {}", username);
            } else {
                log.warn("STOMP CONNECT 인증 실패: 잘못된 토큰");
            }
        }
        return message;
    }

}
