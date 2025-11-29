package com.lingualink.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for joining a WebRTC signaling room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcJoinRoomRequest {
    private String roomId;
    private String userId; // Optional, will use authenticated user if not provided
}

