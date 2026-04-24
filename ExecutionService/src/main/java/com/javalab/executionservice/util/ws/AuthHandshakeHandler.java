package com.javalab.executionservice.util.ws;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class AuthHandshakeHandler extends DefaultHandshakeHandler
{
    @Override
    protected @Nullable Principal determineUser(
            org.springframework.http.server.ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    )
    {
        return (Authentication) attributes.get("auth");
    }
}