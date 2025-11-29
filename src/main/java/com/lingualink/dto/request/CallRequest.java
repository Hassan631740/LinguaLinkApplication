package com.lingualink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Interpreter ID is required")
    private Long interpreterId;

    private Long callDurationSeconds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;

    private String recordingUrl;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private Integer qualityRating; // 1-5
}

