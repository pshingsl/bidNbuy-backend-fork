package com.bidnbuy.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 메세지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 STOMP 엔드포인트
        registry.addEndpoint("/ws/bid")
                // 시큐리티처럼 아래는 모든 도메인에서 접속 가능하도록 허용
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // 메세지 브로커 설정
    // Simple Broker 활성화 (Redis 미사용 시 Spring 내장 메모리 브로커 사용) -> 일단 경매 때문에 추가
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableSimpleBroker("/topic");
        // 클라이언트가 서버의 @Controller로 메시지를 보낼 때 사용하는 접두사
        registry.setApplicationDestinationPrefixes("/app");
    }
}
