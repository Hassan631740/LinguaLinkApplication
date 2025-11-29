package com.lingualink.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WebRTC SDP answer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcAnswerRequest {
    private String roomId;
    private String sdp;
    private String type; // "answer"
    private String targetUserId; // User ID who sent the offer
}

