package com.lingualink.exception;

/**
 * Exception thrown when a user attempts to perform a transaction
 * but has insufficient balance.
 */
public class InsufficientBalanceException extends RuntimeException {
    
    private final String userId;
    private final String requiredAmount;
    private final String availableBalance;

    public InsufficientBalanceException(String userId, String requiredAmount, String availableBalance) {
        super(String.format("Insufficient balance for user %s. Required: %s, Available: %s", 
                userId, requiredAmount, availableBalance));
        this.userId = userId;
        this.requiredAmount = requiredAmount;
        this.availableBalance = availableBalance;
    }

    public InsufficientBalanceException(String message) {
        super(message);
        this.userId = null;
        this.requiredAmount = null;
        this.availableBalance = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getRequiredAmount() {
        return requiredAmount;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }
}

