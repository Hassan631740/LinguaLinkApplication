package com.lingualink.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for interpreter withdrawal request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency = "USD";

    @NotBlank(message = "Account details are required")
    @Size(max = 2000, message = "Account details must not exceed 2000 characters")
    private String accountDetails; // Bank account details, PayPal email, etc.

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}

