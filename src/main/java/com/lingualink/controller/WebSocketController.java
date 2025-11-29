package com.lingualink.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Example WebSocket controller demonstrating JWT-authenticated messaging.
 * All methods are automatically secured by WebSocket security configuration.
 */
@Slf4j
@Controller
public class WebSocketController {

    /**
     * Broadcast message to all subscribers of /topic/messages
     * Example: Client sends to /app/message with body {"text": "Hello"}
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public MessageResponse handleBroadcastMessage(@Payload MessageRequest request, Principal principal) {
        String username = principal != null ? principal.getName() : "anonymous";
        log.info("Received broadcast message from {}: {}", username, request.getText());
        
        return MessageResponse.builder()
                .text(request.getText())
                .sender(username)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Send private message to a specific user
     * Example: Client sends to /app/private with body {"to": "user@example.com", "text": "Hi"}
     * Message will be delivered to /user/{username}/queue/messages
     */
    @MessageMapping("/private")
    @SendToUser("/queue/messages")
    public MessageResponse handlePrivateMessage(@Payload PrivateMessageRequest request, Principal principal) {
        String sender = principal != null ? principal.getName() : "anonymous";
        log.info("Received private message from {} to {}: {}", sender, request.getTo(), request.getText());
        
        return MessageResponse.builder()
                .text(request.getText())
                .sender(sender)
                .recipient(request.getTo())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Echo message back to the sender
     * Example: Client sends to /app/echo with body {"text": "Echo test"}
     */
    @MessageMapping("/echo")
    @SendToUser("/queue/echo")
    public MessageResponse handleEcho(@Payload MessageRequest request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String username = principal != null ? principal.getName() : "anonymous";
        log.info("Received echo request from {}: {}", username, request.getText());
        
        // Access authentication from header accessor if needed
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null) {
            log.debug("User authorities: {}", auth.getAuthorities());
        }
        
        return MessageResponse.builder()
                .text("Echo: " + request.getText())
                .sender("Server")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // DTO classes for message payloads
    public static class MessageRequest {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class PrivateMessageRequest {
        private String to;
        private String text;

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class MessageResponse {
        private String text;
        private String sender;
        private String recipient;
        private LocalDateTime timestamp;
    }
}

