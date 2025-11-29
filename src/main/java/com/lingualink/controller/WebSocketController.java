package com.lingualink.controller;

import com.lingualink.dto.request.*;
import com.lingualink.dto.response.WebRtcSignalResponse;
import com.lingualink.entity.User;
import com.lingualink.repository.UserRepository;
import com.lingualink.service.WebRtcSignalingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket controller for messaging and WebRTC signaling.
 * All methods are automatically secured by WebSocket security configuration.
 */
@Slf4j
@Controller
public class WebSocketController {

    private final WebRtcSignalingService webrtcSignalingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public WebSocketController(WebRtcSignalingService webrtcSignalingService,
                              SimpMessagingTemplate messagingTemplate,
                              UserRepository userRepository) {
        this.webrtcSignalingService = webrtcSignalingService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

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

    // ==================== WebRTC Signaling Handlers ====================

    /**
     * Join a WebRTC signaling room
     * Example: Client sends to /app/webrtc/join with body {"roomId": "room-123"}
     */
    @MessageMapping("/webrtc/join")
    public void handleJoinRoom(@Payload WebRtcJoinRoomRequest request, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("Join room request from unauthenticated user");
            return;
        }

        String roomId = request.getRoomId();
        if (roomId == null || roomId.trim().isEmpty()) {
            log.warn("Invalid room ID: {}", roomId);
            return;
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElse(null);
        String userName = user != null ? (user.getName() != null ? user.getName() : user.getEmail()) : principal.getName();

        log.info("User {} ({}) joining WebRTC room: {}", userId, userName, roomId);
        webrtcSignalingService.joinRoom(roomId, userId, userName);
    }

    /**
     * Leave a WebRTC signaling room
     * Example: Client sends to /app/webrtc/leave with body {"roomId": "room-123"}
     */
    @MessageMapping("/webrtc/leave")
    public void handleLeaveRoom(@Payload WebRtcLeaveRoomRequest request, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("Leave room request from unauthenticated user");
            return;
        }

        String roomId = request.getRoomId();
        if (roomId == null || roomId.trim().isEmpty()) {
            log.warn("Invalid room ID: {}", roomId);
            return;
        }

        log.info("User {} leaving WebRTC room: {}", userId, roomId);
        webrtcSignalingService.leaveRoom(roomId, userId);
    }

    /**
     * Handle WebRTC SDP offer
     * Example: Client sends to /app/webrtc/offer with body {"roomId": "room-123", "sdp": "...", "type": "offer", "targetUserId": "user-456"}
     */
    @MessageMapping("/webrtc/offer")
    public void handleOffer(@Payload WebRtcOfferRequest request, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("Offer request from unauthenticated user");
            return;
        }

        String roomId = request.getRoomId();
        String targetUserId = request.getTargetUserId();

        // Validate room membership
        if (!webrtcSignalingService.isUserInRoom(roomId, userId)) {
            log.warn("User {} attempted to send offer but is not in room {}", userId, roomId);
            return;
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        String userName = user != null ? (user.getName() != null ? user.getName() : user.getEmail()) : principal.getName();

        WebRtcSignalResponse response = WebRtcSignalResponse.builder()
                .type("offer")
                .roomId(roomId)
                .fromUserId(userId)
                .fromUserName(userName)
                .toUserId(targetUserId)
                .sdp(request.getSdp())
                .timestamp(LocalDateTime.now())
                .build();

        // If targetUserId is specified, send to that user only
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            messagingTemplate.convertAndSend("/user/" + targetUserId + "/queue/webrtc-signal", response);
            log.info("Sent WebRTC offer from {} to {} in room {}", userId, targetUserId, roomId);
        } else {
            // Broadcast to all other participants in the room
            webrtcSignalingService.getRoomParticipants(roomId).stream()
                    .filter(participantId -> !participantId.equals(userId))
                    .forEach(participantId -> {
                        messagingTemplate.convertAndSend("/user/" + participantId + "/queue/webrtc-signal", response);
                    });
            log.info("Broadcast WebRTC offer from {} to all participants in room {}", userId, roomId);
        }
    }

    /**
     * Handle WebRTC SDP answer
     * Example: Client sends to /app/webrtc/answer with body {"roomId": "room-123", "sdp": "...", "type": "answer", "targetUserId": "user-456"}
     */
    @MessageMapping("/webrtc/answer")
    public void handleAnswer(@Payload WebRtcAnswerRequest request, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("Answer request from unauthenticated user");
            return;
        }

        String roomId = request.getRoomId();
        String targetUserId = request.getTargetUserId();

        // Validate room membership
        if (!webrtcSignalingService.isUserInRoom(roomId, userId)) {
            log.warn("User {} attempted to send answer but is not in room {}", userId, roomId);
            return;
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        String userName = user != null ? (user.getName() != null ? user.getName() : user.getEmail()) : principal.getName();

        WebRtcSignalResponse response = WebRtcSignalResponse.builder()
                .type("answer")
                .roomId(roomId)
                .fromUserId(userId)
                .fromUserName(userName)
                .toUserId(targetUserId)
                .sdp(request.getSdp())
                .timestamp(LocalDateTime.now())
                .build();

        // Send answer to the user who sent the offer
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            messagingTemplate.convertAndSend("/user/" + targetUserId + "/queue/webrtc-signal", response);
            log.info("Sent WebRTC answer from {} to {} in room {}", userId, targetUserId, roomId);
        } else {
            log.warn("Answer request missing targetUserId from {} in room {}", userId, roomId);
        }
    }

    /**
     * Handle WebRTC ICE candidate
     * Example: Client sends to /app/webrtc/ice-candidate with body {"roomId": "room-123", "candidate": "...", "sdpMid": "...", "sdpMLineIndex": 0, "targetUserId": "user-456"}
     */
    @MessageMapping("/webrtc/ice-candidate")
    public void handleIceCandidate(@Payload WebRtcIceCandidateRequest request, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("ICE candidate request from unauthenticated user");
            return;
        }

        String roomId = request.getRoomId();
        String targetUserId = request.getTargetUserId();

        // Validate room membership
        if (!webrtcSignalingService.isUserInRoom(roomId, userId)) {
            log.warn("User {} attempted to send ICE candidate but is not in room {}", userId, roomId);
            return;
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        String userName = user != null ? (user.getName() != null ? user.getName() : user.getEmail()) : principal.getName();

        WebRtcSignalResponse response = WebRtcSignalResponse.builder()
                .type("ice-candidate")
                .roomId(roomId)
                .fromUserId(userId)
                .fromUserName(userName)
                .toUserId(targetUserId)
                .candidate(request.getCandidate())
                .sdpMid(request.getSdpMid())
                .sdpMLineIndex(request.getSdpMLineIndex())
                .timestamp(LocalDateTime.now())
                .build();

        // Send ICE candidate to the target user
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            messagingTemplate.convertAndSend("/user/" + targetUserId + "/queue/webrtc-signal", response);
            log.debug("Sent WebRTC ICE candidate from {} to {} in room {}", userId, targetUserId, roomId);
        } else {
            // Broadcast to all other participants in the room
            webrtcSignalingService.getRoomParticipants(roomId).stream()
                    .filter(participantId -> !participantId.equals(userId))
                    .forEach(participantId -> {
                        messagingTemplate.convertAndSend("/user/" + participantId + "/queue/webrtc-signal", response);
                    });
            log.debug("Broadcast WebRTC ICE candidate from {} to all participants in room {}", userId, roomId);
        }
    }

    /**
     * Get room status
     * Example: Client sends to /app/webrtc/room-status with body {"roomId": "room-123"}
     */
    @MessageMapping("/webrtc/room-status")
    @SendToUser("/queue/webrtc-room-status")
    public com.lingualink.dto.response.WebRtcRoomStatusResponse handleRoomStatus(@Payload WebRtcJoinRoomRequest request, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        if (userId == null) {
            log.warn("Room status request from unauthenticated user");
            return null;
        }

        String roomId = request.getRoomId();
        if (roomId == null || roomId.trim().isEmpty()) {
            log.warn("Invalid room ID: {}", roomId);
            return null;
        }

        log.debug("User {} requesting status for room {}", userId, roomId);
        return webrtcSignalingService.getRoomStatus(roomId);
    }

    /**
     * Helper method to extract user ID from Principal
     * The Principal name is the email, so we need to get the user ID from the repository
     */
    private String getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }

        String email = principal.getName();
        return userRepository.findByEmail(email)
                .map(user -> String.valueOf(user.getId()))
                .orElse(null);
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

