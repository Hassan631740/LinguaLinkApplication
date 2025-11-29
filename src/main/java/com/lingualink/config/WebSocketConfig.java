package com.lingualink.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

/**
 * WebSocket configuration with STOMP messaging protocol support.
 * Enables JWT authentication on WebSocket handshake.
 * Authentication is validated during handshake, and all subsequent messages
 * come from authenticated users only.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtWebSocketHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(JwtWebSocketHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic" or "/queue"
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // Prefix for messages that are bound to @MessageMapping methods
        // This means if client sends to "/app/hello", it will be routed to @MessageMapping("/hello")
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        // Messages sent to "/user/{username}/queue/messages" will be sent to the specific user
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint at "/ws"
        // Client connects to: ws://localhost:8080/ws?token=JWT_TOKEN
        // or ws://localhost:8080/ws with Authorization header
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Configure allowed origins for production
                .addInterceptors(jwtHandshakeInterceptor)
                .withSockJS(); // Enable SockJS fallback for older browsers
        
        // Also register without SockJS for native WebSocket connections
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*")
                .addInterceptors(jwtHandshakeInterceptor);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add interceptor to validate that messages come from authenticated users
        // Since authentication is validated during handshake, this primarily logs and validates
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null) {
                    Principal principal = accessor.getUser();
                    
                    // For CONNECT commands, authentication is already validated in handshake
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        if (principal != null) {
                            log.info("STOMP CONNECT from authenticated user: {}", principal.getName());
                        } else {
                            log.warn("STOMP CONNECT without authentication - should not happen");
                        }
                    }
                    
                    // For other commands, verify user is authenticated
                    if (!StompCommand.CONNECT.equals(accessor.getCommand()) && 
                        !StompCommand.DISCONNECT.equals(accessor.getCommand()) &&
                        principal == null) {
                        log.warn("Message from unauthenticated user - rejecting. Command: {}", accessor.getCommand());
                        return null; // Reject message
                    }
                }
                
                return message;
            }
        });
    }
}

