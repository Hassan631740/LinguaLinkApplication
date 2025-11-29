package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.PaymentRequest;
import com.lingualink.dto.response.PaymentResponse;
import com.lingualink.service.PaymentService;
import com.lingualink.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new payment", description = "Creates a new payment record in the system (Client or Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse createdPayment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/payments/" + createdPayment.getId())
                .body(createdPayment);
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieves a paginated list of all payments with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of payments")
    })
    public ResponseEntity<?> getAllPayments(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) Long bookingId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by currency") @RequestParam(required = false) String currency,
            @Parameter(description = "Filter by payment method") @RequestParam(required = false) String method,
            @Parameter(description = "Filter by minimum amount") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Filter by maximum amount") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Filter by paid date from (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime paidFrom,
            @Parameter(description = "Filter by paid date to (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime paidTo) {
        
        if (page != null || size != null || bookingId != null || status != null || 
            currency != null || method != null || minAmount != null || maxAmount != null || 
            paidFrom != null || paidTo != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<PaymentResponse> pagedResponse = paymentService.getAllPaymentsWithFiltersAsResponse(
                    pageParams, bookingId, status, currency, method, minAmount, maxAmount, paidFrom, paidTo);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<PaymentResponse> payments = paymentService.getAllPaymentsAsResponse();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentByIdAsResponse(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payments by booking ID", description = "Retrieves all payments for a specific booking with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved payments")
    public ResponseEntity<?> getPaymentsByBookingId(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<PaymentResponse> pagedResponse = paymentService.getPaymentsByBookingIdAsResponse(bookingId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<PaymentResponse> payments = paymentService.getPaymentsByBookingIdAsResponse(bookingId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status", description = "Retrieves all payments with a specific status with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved payments")
    public ResponseEntity<?> getPaymentsByStatus(
            @Parameter(description = "Payment status") @PathVariable String status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<PaymentResponse> pagedResponse = paymentService.getPaymentsByStatusAsResponse(status, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<PaymentResponse> payments = paymentService.getPaymentsByStatusAsResponse(status);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Update payment", description = "Fully updates an existing payment (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PaymentResponse> updatePayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse updatedPayment = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(updatedPayment);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Partially update payment", description = "Partially updates an existing payment (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<PaymentResponse> patchPayment(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse updatedPayment = paymentService.patchPayment(id, request);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete payment", description = "Deletes a payment by its ID (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Payment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deletePayment(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
