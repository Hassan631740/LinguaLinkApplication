package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallResponse {
    private Long id;
    private Long bookingId;
    private String bookingTitle;
    private Long clientId;
    private String clientName;
    private Long interpreterId;
    private String interpreterName;
    private Long callDurationSeconds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String recordingUrl;
    private String notes;
    private Integer qualityRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

