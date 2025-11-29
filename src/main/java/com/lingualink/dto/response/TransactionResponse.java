package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long bookingId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private String paymentMethod;
    private String paymentReference;
    private String withdrawalAccountDetails;
    private LocalDateTime processedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

