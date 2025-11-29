package com.lingualink.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WebRTC ICE candidate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcIceCandidateRequest {
    private String roomId;
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
    private String targetUserId; // User ID to send the ICE candidate to
}

