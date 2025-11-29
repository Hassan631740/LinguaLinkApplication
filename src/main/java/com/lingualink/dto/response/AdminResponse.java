package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String department;
    private String employeeId;
    private String accessLevel;
    private LocalDateTime lastLogin;
    private Boolean isActive;
    private String permissions;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

