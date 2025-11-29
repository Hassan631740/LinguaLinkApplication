package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for WebRTC room status and participant list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebRtcRoomStatusResponse {
    private String roomId;
    private List<ParticipantInfo> participants;
    private int participantCount;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInfo {
        private String userId;
        private String userName;
        private String userEmail;
        private Boolean isConnected;
        private Long joinedAt; // Timestamp
    }
}

