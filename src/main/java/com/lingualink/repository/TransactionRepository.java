package com.lingualink.repository;

import com.lingualink.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find transactions by user
    Page<Transaction> findByUser_Id(Long userId, Pageable pageable);
    List<Transaction> findByUser_Id(Long userId);
    
    // Find transactions by booking
    List<Transaction> findByBooking_Id(Long bookingId);
    
    // Find transactions by type
    Page<Transaction> findByTransactionType(Transaction.TransactionType transactionType, Pageable pageable);
    List<Transaction> findByTransactionType(Transaction.TransactionType transactionType);
    
    // Find transactions by status
    Page<Transaction> findByStatus(Transaction.TransactionStatus status, Pageable pageable);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    // Find transactions by user and type
    Page<Transaction> findByUser_IdAndTransactionType(Long userId, Transaction.TransactionType transactionType, Pageable pageable);
    List<Transaction> findByUser_IdAndTransactionType(Long userId, Transaction.TransactionType transactionType);
    
    // Find transactions by user and status
    Page<Transaction> findByUser_IdAndStatus(Long userId, Transaction.TransactionStatus status, Pageable pageable);
    List<Transaction> findByUser_IdAndStatus(Long userId, Transaction.TransactionStatus status);
    
    // Find withdrawal requests (pending)
    Page<Transaction> findByTransactionTypeAndStatus(
            Transaction.TransactionType transactionType,
            Transaction.TransactionStatus status,
            Pageable pageable
    );
    
    // Calculate total earnings for interpreter (completed bookings only)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionType = 'EARNING' " +
           "AND t.status = 'COMPLETED'")
    BigDecimal calculateTotalEarnings(@Param("userId") Long userId);
    
    // Calculate total withdrawals for interpreter (completed withdrawals)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.transactionType IN ('WITHDRAWAL_COMPLETED') " +
           "AND t.status = 'COMPLETED'")
    BigDecimal calculateTotalWithdrawals(@Param("userId") Long userId);
    
    // Find transactions within date range
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    Page<Transaction> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}

