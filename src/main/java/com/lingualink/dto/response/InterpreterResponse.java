package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String languages;
    private BigDecimal ratePerHour;
    private Integer experienceYears;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

