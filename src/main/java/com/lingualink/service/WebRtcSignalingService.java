package com.lingualink.service;

import com.lingualink.dto.response.WebRtcRoomStatusResponse;
import com.lingualink.entity.User;
import com.lingualink.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing WebRTC signaling rooms and participant tracking.
 * Handles room-based signaling for peer-to-peer connections.
 */
@Slf4j
@Service
public class WebRtcSignalingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    
    // Map of roomId -> Set of userIds in that room
    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();
    
    // Map of userId -> Set of roomIds the user is in
    private final Map<String, Set<String>> userRooms = new ConcurrentHashMap<>();
    
    // Map of userId -> participant info (connection status, join time)
    private final Map<String, ParticipantInfo> participants = new ConcurrentHashMap<>();

    public WebRtcSignalingService(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    /**
     * Add a user to a signaling room
     */
    public void joinRoom(String roomId, String userId, String userName) {
        log.info("User {} ({}) joining room: {}", userId, userName, roomId);
        
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        userRooms.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(roomId);
        participants.put(userId, new ParticipantInfo(userId, userName, true, Instant.now().toEpochMilli()));
        
        // Notify other participants in the room
        notifyParticipantJoined(roomId, userId, userName);
        
        // Send current room status to the joining user
        sendRoomStatus(roomId, userId);
    }

    /**
     * Remove a user from a signaling room
     */
    public void leaveRoom(String roomId, String userId) {
        log.info("User {} leaving room: {}", userId, roomId);
        
        Set<String> roomParticipants = rooms.get(roomId);
        if (roomParticipants != null) {
            roomParticipants.remove(userId);
            if (roomParticipants.isEmpty()) {
                rooms.remove(roomId);
            }
        }
        
        Set<String> userRoomSet = userRooms.get(userId);
        if (userRoomSet != null) {
            userRoomSet.remove(roomId);
            if (userRoomSet.isEmpty()) {
                userRooms.remove(userId);
            }
        }
        
        participants.remove(userId);
        
        // Notify other participants in the room
        notifyParticipantLeft(roomId, userId);
    }

    /**
     * Remove user from all rooms (on disconnect)
     */
    public void disconnectUser(String userId) {
        log.info("User {} disconnecting from all rooms", userId);
        
        Set<String> userRoomSet = userRooms.remove(userId);
        if (userRoomSet != null) {
            for (String roomId : userRoomSet) {
                Set<String> roomParticipants = rooms.get(roomId);
                if (roomParticipants != null) {
                    roomParticipants.remove(userId);
                    if (roomParticipants.isEmpty()) {
                        rooms.remove(roomId);
                    } else {
                        // Notify remaining participants
                        notifyParticipantLeft(roomId, userId);
                    }
                }
            }
        }
        
        participants.remove(userId);
    }

    /**
     * Check if a user is in a room
     */
    public boolean isUserInRoom(String roomId, String userId) {
        Set<String> roomParticipants = rooms.get(roomId);
        return roomParticipants != null && roomParticipants.contains(userId);
    }

    /**
     * Get all participants in a room
     */
    public Set<String> getRoomParticipants(String roomId) {
        Set<String> participants = rooms.get(roomId);
        return participants != null ? new HashSet<>(participants) : Collections.emptySet();
    }

    /**
     * Get room status with participant information
     */
    public WebRtcRoomStatusResponse getRoomStatus(String roomId) {
        Set<String> participantIds = rooms.get(roomId);
        if (participantIds == null || participantIds.isEmpty()) {
            return WebRtcRoomStatusResponse.builder()
                    .roomId(roomId)
                    .participants(Collections.emptyList())
                    .participantCount(0)
                    .build();
        }

        List<WebRtcRoomStatusResponse.ParticipantInfo> participantInfos = participantIds.stream()
                .map(userId -> {
                    ParticipantInfo info = participants.get(userId);
                    if (info != null) {
                        // Fetch user details from repository
                        Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
                        String email = userOpt.map(User::getEmail).orElse("Unknown");
                        
                        return WebRtcRoomStatusResponse.ParticipantInfo.builder()
                                .userId(info.userId)
                                .userName(info.userName)
                                .userEmail(email)
                                .isConnected(info.isConnected)
                                .joinedAt(info.joinedAt)
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return WebRtcRoomStatusResponse.builder()
                .roomId(roomId)
                .participants(participantInfos)
                .participantCount(participantInfos.size())
                .build();
    }

    /**
     * Notify all participants in a room (except sender) that someone joined
     */
    private void notifyParticipantJoined(String roomId, String userId, String userName) {
        Set<String> roomParticipants = rooms.get(roomId);
        if (roomParticipants != null) {
            for (String participantId : roomParticipants) {
                if (!participantId.equals(userId)) {
                    messagingTemplate.convertAndSend(
                            "/user/" + participantId + "/queue/webrtc-signal",
                            com.lingualink.dto.response.WebRtcSignalResponse.builder()
                                    .type("participant-joined")
                                    .roomId(roomId)
                                    .fromUserId(userId)
                                    .fromUserName(userName)
                                    .timestamp(java.time.LocalDateTime.now())
                                    .build()
                    );
                }
            }
        }
    }

    /**
     * Notify all participants in a room that someone left
     */
    private void notifyParticipantLeft(String roomId, String userId) {
        Set<String> roomParticipants = rooms.get(roomId);
        if (roomParticipants != null) {
            for (String participantId : roomParticipants) {
                messagingTemplate.convertAndSend(
                        "/user/" + participantId + "/queue/webrtc-signal",
                        com.lingualink.dto.response.WebRtcSignalResponse.builder()
                                .type("participant-left")
                                .roomId(roomId)
                                .fromUserId(userId)
                                .timestamp(java.time.LocalDateTime.now())
                                .build()
                );
            }
        }
    }

    /**
     * Send current room status to a specific user
     */
    private void sendRoomStatus(String roomId, String userId) {
        WebRtcRoomStatusResponse status = getRoomStatus(roomId);
        messagingTemplate.convertAndSend("/user/" + userId + "/queue/webrtc-room-status", status);
    }

    /**
     * Internal class to track participant information
     */
    private static class ParticipantInfo {
        final String userId;
        final String userName;
        final boolean isConnected;
        final long joinedAt;

        ParticipantInfo(String userId, String userName, boolean isConnected, long joinedAt) {
            this.userId = userId;
            this.userName = userName;
            this.isConnected = isConnected;
            this.joinedAt = joinedAt;
        }
    }
}

