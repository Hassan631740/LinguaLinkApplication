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
 * Request DTO for client balance top-up
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency = "USD";

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod; // e.g., "CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"

    @Size(max = 255, message = "Payment reference must not exceed 255 characters")
    private String paymentReference; // External payment gateway transaction ID

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}

