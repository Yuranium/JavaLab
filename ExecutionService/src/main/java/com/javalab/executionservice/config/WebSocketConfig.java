package com.javalab.executionservice.config;

import com.javalab.executionservice.service.ExecutionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer
{
    private final ExecutionWebSocketHandler executionWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(executionWebSocketHandler, "/ws/execution")
                .setAllowedOrigins("*");
    }
}