package com.lingualink.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WebRTC SDP offer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcOfferRequest {
    private String roomId;
    private String sdp;
    private String type; // "offer"
    private String targetUserId; // User ID to send the offer to (optional, for direct calls)
}

