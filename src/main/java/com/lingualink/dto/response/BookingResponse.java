package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private Long interpreterId;
    private String interpreterName;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime confirmedAt;
    private BigDecimal price;
    private Long paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

