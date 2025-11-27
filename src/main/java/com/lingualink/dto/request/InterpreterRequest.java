package com.lingualink.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Languages are required")
    @Size(max = 1000, message = "Languages must not exceed 1000 characters")
    private String languages;

    @NotNull(message = "Rate per hour is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate per hour must be positive")
    private BigDecimal ratePerHour;

    @Min(value = 0, message = "Experience years must be non-negative")
    private Integer experienceYears;

    @Size(max = 5000, message = "Bio must not exceed 5000 characters")
    private String bio;
}

