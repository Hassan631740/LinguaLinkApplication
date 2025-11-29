package com.lingualink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Size(max = 50, message = "Employee ID must not exceed 50 characters")
    private String employeeId;

    @Size(max = 50, message = "Access level must not exceed 50 characters")
    private String accessLevel; // SUPER_ADMIN, ADMIN, MODERATOR

    private Boolean isActive = true;

    @Size(max = 1000, message = "Permissions must not exceed 1000 characters")
    private String permissions; // JSON string of permissions

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}

