package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.TopUpRequest;
import com.lingualink.dto.request.WithdrawalRequest;
import com.lingualink.dto.response.TransactionResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Role;
import com.lingualink.entity.Transaction;
import com.lingualink.entity.User;
import com.lingualink.exception.InsufficientBalanceException;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.TransactionMapper;
import com.lingualink.repository.BookingRepository;
import com.lingualink.repository.TransactionRepository;
import com.lingualink.repository.UserRepository;
import com.lingualink.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing financial transactions including:
 * - Client balance top-ups
 * - Balance deductions for bookings
 * - Interpreter earnings
 * - Interpreter withdrawal requests
 */
@Slf4j
@Service
@Transactional
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public TransactionService(TransactionRepository transactionRepository,
                             TransactionMapper transactionMapper,
                             UserRepository userRepository,
                             BookingRepository bookingRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Top up client balance
     */
    public TransactionResponse topUpBalance(TopUpRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResourceNotFoundException("User", "id", "current");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Verify user is a client
        if (user.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("Only clients can top up their balance");
        }

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal newBalance = balanceBefore.add(request.getAmount());
        user.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.TOP_UP);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setPaymentReference(request.getPaymentReference());
        transaction.setNotes(request.getNotes());
        transaction.setProcessedAt(LocalDateTime.now());

        userRepository.save(user);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Balance top-up completed for user {}: {} {}, Balance: {} -> {}", 
                user.getId(), request.getAmount(), request.getCurrency(), balanceBefore, newBalance);

        return transactionMapper.toResponse(savedTransaction);
    }

    /**
     * Deduct balance for a booking (called after call completion)
     */
    public TransactionResponse deductBalance(Long bookingId, BigDecimal amount, String currency) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Get the event organizer (client who needs to pay)
        User client = booking.getEvent().getOrganizer();
        if (client == null) {
            throw new ResourceNotFoundException("Event organizer", "bookingId", bookingId);
        }

        // Verify user is a client
        if (client.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("Only clients can have balance deducted");
        }

        BigDecimal balanceBefore = client.getBalance();
        
        // Check if sufficient balance
        if (balanceBefore.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.valueOf(client.getId()),
                    amount.toString(),
                    balanceBefore.toString()
            );
        }

        BigDecimal newBalance = balanceBefore.subtract(amount);
        client.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setUser(client);
        transaction.setBooking(booking);
        transaction.setTransactionType(Transaction.TransactionType.DEDUCTION);
        transaction.setAmount(amount);
        transaction.setCurrency(currency != null ? currency : "USD");
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setPaymentMethod("WALLET");
        transaction.setNotes("Balance deduction for booking #" + bookingId);
        transaction.setProcessedAt(LocalDateTime.now());

        userRepository.save(client);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Balance deducted for user {} (booking {}): {} {}, Balance: {} -> {}", 
                client.getId(), bookingId, amount, currency, balanceBefore, newBalance);

        return transactionMapper.toResponse(savedTransaction);
    }

    /**
     * Record interpreter earnings from a completed booking
     */
    public TransactionResponse recordEarning(Long bookingId, BigDecimal amount, String currency) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getInterpreter() == null) {
            throw new ResourceNotFoundException("Interpreter", "bookingId", bookingId);
        }

        User interpreter = booking.getInterpreter().getUser();
        if (interpreter == null) {
            throw new ResourceNotFoundException("Interpreter user", "bookingId", bookingId);
        }

        BigDecimal balanceBefore = interpreter.getBalance();
        BigDecimal newBalance = balanceBefore.add(amount);
        interpreter.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setUser(interpreter);
        transaction.setBooking(booking);
        transaction.setTransactionType(Transaction.TransactionType.EARNING);
        transaction.setAmount(amount);
        transaction.setCurrency(currency != null ? currency : "USD");
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setPaymentMethod("BOOKING_EARNING");
        transaction.setNotes("Earning from completed booking #" + bookingId);
        transaction.setProcessedAt(LocalDateTime.now());

        userRepository.save(interpreter);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Earning recorded for interpreter {} (booking {}): {} {}, Balance: {} -> {}", 
                interpreter.getId(), bookingId, amount, currency, balanceBefore, newBalance);

        return transactionMapper.toResponse(savedTransaction);
    }

    /**
     * Create withdrawal request for interpreter
     */
    public TransactionResponse requestWithdrawal(WithdrawalRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResourceNotFoundException("User", "id", "current");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Verify user is an interpreter
        if (user.getRole() != Role.INTERPRETER) {
            throw new IllegalArgumentException("Only interpreters can request withdrawals");
        }

        BigDecimal balanceBefore = user.getBalance();
        
        // Check if sufficient balance
        if (balanceBefore.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    String.valueOf(user.getId()),
                    request.getAmount().toString(),
                    balanceBefore.toString()
            );
        }

        // Reserve the amount by deducting from balance
        BigDecimal newBalance = balanceBefore.subtract(request.getAmount());
        user.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL_REQUEST);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setWithdrawalAccountDetails(request.getAccountDetails());
        transaction.setNotes(request.getNotes());

        userRepository.save(user);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Withdrawal request created for interpreter {}: {} {}, Balance: {} -> {}", 
                user.getId(), request.getAmount(), request.getCurrency(), balanceBefore, newBalance);

        return transactionMapper.toResponse(savedTransaction);
    }

    /**
     * Approve and complete a withdrawal request (admin only)
     */
    public TransactionResponse approveWithdrawal(Long transactionId) {
        Transaction withdrawalRequest = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (withdrawalRequest.getTransactionType() != Transaction.TransactionType.WITHDRAWAL_REQUEST) {
            throw new IllegalArgumentException("Transaction is not a withdrawal request");
        }

        if (withdrawalRequest.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Withdrawal request is not pending");
        }

        withdrawalRequest.setStatus(Transaction.TransactionStatus.COMPLETED);
        withdrawalRequest.setProcessedAt(LocalDateTime.now());

        // Create a completion record
        Transaction completionTransaction = new Transaction();
        completionTransaction.setUser(withdrawalRequest.getUser());
        completionTransaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL_COMPLETED);
        completionTransaction.setAmount(withdrawalRequest.getAmount());
        completionTransaction.setCurrency(withdrawalRequest.getCurrency());
        completionTransaction.setBalanceBefore(withdrawalRequest.getBalanceAfter());
        completionTransaction.setBalanceAfter(withdrawalRequest.getBalanceAfter()); // Balance already deducted
        completionTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        completionTransaction.setWithdrawalAccountDetails(withdrawalRequest.getWithdrawalAccountDetails());
        completionTransaction.setNotes("Withdrawal completed for transaction #" + transactionId);
        completionTransaction.setProcessedAt(LocalDateTime.now());

        transactionRepository.save(withdrawalRequest);
        Transaction savedCompletion = transactionRepository.save(completionTransaction);

        log.info("Withdrawal approved and completed for transaction {}", transactionId);

        return transactionMapper.toResponse(savedCompletion);
    }

    /**
     * Reject a withdrawal request (admin only)
     */
    public TransactionResponse rejectWithdrawal(Long transactionId, String reason) {
        Transaction withdrawalRequest = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (withdrawalRequest.getTransactionType() != Transaction.TransactionType.WITHDRAWAL_REQUEST) {
            throw new IllegalArgumentException("Transaction is not a withdrawal request");
        }

        if (withdrawalRequest.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Withdrawal request is not pending");
        }

        // Refund the amount back to interpreter's balance
        User user = withdrawalRequest.getUser();
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal refundAmount = withdrawalRequest.getAmount();
        BigDecimal newBalance = balanceBefore.add(refundAmount);
        user.setBalance(newBalance);

        withdrawalRequest.setStatus(Transaction.TransactionStatus.FAILED);
        withdrawalRequest.setProcessedAt(LocalDateTime.now());
        withdrawalRequest.setNotes(withdrawalRequest.getNotes() + " | Rejected: " + reason);

        // Create rejection record
        Transaction rejectionTransaction = new Transaction();
        rejectionTransaction.setUser(user);
        rejectionTransaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL_REJECTED);
        rejectionTransaction.setAmount(refundAmount);
        rejectionTransaction.setCurrency(withdrawalRequest.getCurrency());
        rejectionTransaction.setBalanceBefore(balanceBefore);
        rejectionTransaction.setBalanceAfter(newBalance);
        rejectionTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        rejectionTransaction.setNotes("Withdrawal rejected and refunded. Reason: " + reason);
        rejectionTransaction.setProcessedAt(LocalDateTime.now());

        userRepository.save(user);
        transactionRepository.save(withdrawalRequest);
        Transaction savedRejection = transactionRepository.save(rejectionTransaction);

        log.info("Withdrawal rejected for transaction {}. Amount refunded to user {}", transactionId, user.getId());

        return transactionMapper.toResponse(savedRejection);
    }

    /**
     * Get user's current balance
     */
    @Transactional(readOnly = true)
    public BigDecimal getUserBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return user.getBalance();
    }

    /**
     * Get all transactions for current user
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getUserTransactions(PageParams pageParams) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResourceNotFoundException("User", "id", "current");
        }

        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Transaction> page = transactionRepository.findByUser_Id(currentUserId, pageable);
        List<TransactionResponse> content = page.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    /**
     * Get all transactions for a specific user (admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getUserTransactions(Long userId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Transaction> page = transactionRepository.findByUser_Id(userId, pageable);
        List<TransactionResponse> content = page.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    /**
     * Get all pending withdrawal requests (admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getPendingWithdrawals(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Transaction> page = transactionRepository.findByTransactionTypeAndStatus(
                Transaction.TransactionType.WITHDRAWAL_REQUEST,
                Transaction.TransactionStatus.PENDING,
                pageable
        );
        List<TransactionResponse> content = page.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return transactionMapper.toResponse(transaction);
    }

    /**
     * Calculate total earnings for an interpreter
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalEarnings(Long userId) {
        return transactionRepository.calculateTotalEarnings(userId);
    }

    /**
     * Calculate total withdrawals for an interpreter
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalWithdrawals(Long userId) {
        return transactionRepository.calculateTotalWithdrawals(userId);
    }

    /**
     * Get available balance (earnings - withdrawals) for an interpreter
     */
    @Transactional(readOnly = true)
    public BigDecimal getAvailableBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return user.getBalance();
    }
}

