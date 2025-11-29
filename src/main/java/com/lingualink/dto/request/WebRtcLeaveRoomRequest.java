package com.lingualink.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for leaving a WebRTC signaling room
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcLeaveRoomRequest {
    private String roomId;
}

