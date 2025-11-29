package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for WebRTC signaling responses (offer, answer, ICE candidate)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebRtcSignalResponse {
    private String type; // "offer", "answer", "ice-candidate", "participant-joined", "participant-left"
    private String roomId;
    private String fromUserId;
    private String fromUserName;
    private String toUserId;
    private String sdp; // For offer/answer
    private String candidate; // For ICE candidate
    private String sdpMid; // For ICE candidate
    private Integer sdpMLineIndex; // For ICE candidate
    private LocalDateTime timestamp;
}

