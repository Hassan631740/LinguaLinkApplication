package com.lingualink.mapper;

import com.lingualink.dto.response.TransactionResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Transaction;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        User user = transaction.getUser();
        
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(user != null ? user.getId() : null)
                .userName(user != null ? (user.getName() != null ? user.getName() : user.getEmail()) : null)
                .userEmail(user != null ? user.getEmail() : null)
                .bookingId(transaction.getBooking() != null ? transaction.getBooking().getId() : null)
                .transactionType(transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .paymentMethod(transaction.getPaymentMethod())
                .paymentReference(transaction.getPaymentReference())
                .withdrawalAccountDetails(transaction.getWithdrawalAccountDetails())
                .processedAt(transaction.getProcessedAt())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}

