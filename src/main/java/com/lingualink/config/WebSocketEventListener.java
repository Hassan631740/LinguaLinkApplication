package com.lingualink.config;

import com.lingualink.repository.UserRepository;
import com.lingualink.service.WebRtcSignalingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

/**
 * Event listener for WebSocket connection lifecycle events.
 * Tracks participant statuses and cleans up resources on disconnect.
 */
@Slf4j
@Component
public class WebSocketEventListener {

    private final WebRtcSignalingService webrtcSignalingService;
    private final UserRepository userRepository;

    public WebSocketEventListener(WebRtcSignalingService webrtcSignalingService, UserRepository userRepository) {
        this.webrtcSignalingService = webrtcSignalingService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        
        if (principal != null) {
            log.info("WebSocket session connected for user: {}", principal.getName());
        } else {
            log.warn("WebSocket session connected without authentication");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        
        if (principal != null) {
            String email = principal.getName();
            log.info("WebSocket session disconnected for user: {}", email);
            
            // Convert email to userId and disconnect from all WebRTC rooms
            userRepository.findByEmail(email).ifPresent(user -> {
                String userId = String.valueOf(user.getId());
                webrtcSignalingService.disconnectUser(userId);
                log.info("Cleaned up WebRTC rooms for disconnected user: {} (ID: {})", email, userId);
            });
        } else {
            log.warn("WebSocket session disconnected without authentication");
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Principal principal = headerAccessor.getUser();
        
        if (principal != null) {
            log.debug("User {} subscribed to: {}", principal.getName(), destination);
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        Principal principal = headerAccessor.getUser();
        
        if (principal != null) {
            log.debug("User {} unsubscribed from: {}", principal.getName(), destination);
        }
    }
}

