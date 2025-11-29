package com.lingualink.controller;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.TopUpRequest;
import com.lingualink.dto.request.WithdrawalRequest;
import com.lingualink.dto.response.TransactionResponse;
import com.lingualink.service.TransactionService;
import com.lingualink.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/top-up")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Top up client balance", description = "Add funds to client's wallet balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Balance topped up successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "402", description = "Insufficient balance (should not occur for top-up)"),
            @ApiResponse(responseCode = "403", description = "Access denied - only clients can top up")
    })
    public ResponseEntity<TransactionResponse> topUpBalance(@Valid @RequestBody TopUpRequest request) {
        TransactionResponse transaction = transactionService.topUpBalance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/transactions/" + transaction.getId())
                .body(transaction);
    }

    @PostMapping("/withdrawals")
    @PreAuthorize("hasRole('INTERPRETER')")
    @Operation(summary = "Request withdrawal", description = "Interpreter requests withdrawal of earnings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Withdrawal request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "402", description = "Insufficient balance"),
            @ApiResponse(responseCode = "403", description = "Access denied - only interpreters can request withdrawals")
    })
    public ResponseEntity<TransactionResponse> requestWithdrawal(@Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse transaction = transactionService.requestWithdrawal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/transactions/" + transaction.getId())
                .body(transaction);
    }

    @GetMapping("/balance")
    @Operation(summary = "Get current balance", description = "Get the current authenticated user's balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getBalance() {
        Long userId = com.lingualink.security.SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BigDecimal balance = transactionService.getUserBalance(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "balance", balance));
    }

    @GetMapping("/balance/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get user balance (Admin)", description = "Get balance for a specific user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only")
    })
    public ResponseEntity<Map<String, Object>> getUserBalance(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        BigDecimal balance = transactionService.getUserBalance(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "balance", balance));
    }

    @GetMapping
    @Operation(summary = "Get user transactions", description = "Get all transactions for the current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    })
    public ResponseEntity<PagedResponse<TransactionResponse>> getUserTransactions(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        PageParams pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
        PagedResponse<TransactionResponse> transactions = transactionService.getUserTransactions(pageParams);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get transactions for user (Admin)", description = "Get all transactions for a specific user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only")
    })
    public ResponseEntity<PagedResponse<TransactionResponse>> getUserTransactions(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        PageParams pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
        PagedResponse<TransactionResponse> transactions = transactionService.getUserTransactions(userId, pageParams);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/withdrawals/pending")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Get pending withdrawals (Admin)", description = "Get all pending withdrawal requests (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending withdrawals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only")
    })
    public ResponseEntity<PagedResponse<TransactionResponse>> getPendingWithdrawals(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        PageParams pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
        PagedResponse<TransactionResponse> withdrawals = transactionService.getPendingWithdrawals(pageParams);
        return ResponseEntity.ok(withdrawals);
    }

    @PostMapping("/withdrawals/{transactionId}/approve")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Approve withdrawal (Admin)", description = "Approve and complete a withdrawal request (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal approved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid withdrawal request"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> approveWithdrawal(
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId) {
        TransactionResponse transaction = transactionService.approveWithdrawal(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/withdrawals/{transactionId}/reject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Reject withdrawal (Admin)", description = "Reject a withdrawal request and refund the amount (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal rejected and refunded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid withdrawal request"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> rejectWithdrawal(
            @Parameter(description = "Transaction ID") @PathVariable Long transactionId,
            @Parameter(description = "Rejection reason") @RequestBody(required = false) Map<String, String> request) {
        String reason = request != null && request.containsKey("reason") ? request.get("reason") : "No reason provided";
        TransactionResponse transaction = transactionService.rejectWithdrawal(transactionId, reason);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Get a specific transaction by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - users can only view their own transactions"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable Long id) {
        TransactionResponse transaction = transactionService.getTransactionById(id);
        
        // Check if user can access this transaction (own transaction or admin)
        Long currentUserId = com.lingualink.security.SecurityUtils.getCurrentUserId();
        boolean isAdmin = com.lingualink.security.SecurityUtils.isAdministrator();
        
        if (!isAdmin && (transaction.getUserId() == null || !transaction.getUserId().equals(currentUserId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/earnings/total")
    @PreAuthorize("hasRole('INTERPRETER')")
    @Operation(summary = "Get total earnings (Interpreter)", description = "Get total earnings for the current interpreter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total earnings retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - only interpreters")
    })
    public ResponseEntity<Map<String, Object>> getTotalEarnings() {
        Long userId = com.lingualink.security.SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BigDecimal totalEarnings = transactionService.calculateTotalEarnings(userId);
        BigDecimal totalWithdrawals = transactionService.calculateTotalWithdrawals(userId);
        BigDecimal availableBalance = transactionService.getAvailableBalance(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalEarnings", totalEarnings,
                "totalWithdrawals", totalWithdrawals,
                "availableBalance", availableBalance
        ));
    }
}

