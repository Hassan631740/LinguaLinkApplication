package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity to track all financial movements:
 * - Client balance top-ups
 * - Balance deductions for bookings
 * - Interpreter earnings
 * - Interpreter withdrawal requests and completions
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 10, nullable = false)
    private String currency = "USD";

    @Column(name = "balance_before", precision = 10, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TransactionStatus status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // e.g., "CREDIT_CARD", "BANK_TRANSFER", "PAYPAL", "WALLET"

    @Column(name = "payment_reference", length = 255)
    private String paymentReference; // External payment reference ID

    @Column(name = "withdrawal_account_details", columnDefinition = "TEXT")
    private String withdrawalAccountDetails; // For withdrawal requests - bank details, etc.

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum TransactionType {
        TOP_UP,              // Client adding funds to balance
        DEDUCTION,           // Client balance deduction for booking
        EARNING,             // Interpreter earning from completed booking
        WITHDRAWAL_REQUEST,  // Interpreter requesting withdrawal
        WITHDRAWAL_COMPLETED,// Withdrawal request processed
        WITHDRAWAL_REJECTED, // Withdrawal request rejected
        REFUND               // Refund for cancelled booking
    }

    public enum TransactionStatus {
        PENDING,     // Transaction is pending
        COMPLETED,   // Transaction completed successfully
        FAILED,      // Transaction failed
        CANCELLED    // Transaction was cancelled
    }
}

