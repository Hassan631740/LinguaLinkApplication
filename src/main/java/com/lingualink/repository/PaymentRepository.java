package com.lingualink.repository;

import com.lingualink.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBooking_Id(Long bookingId);
    List<Payment> findByStatus(String status);
    
    // Pagination support
    Page<Payment> findAll(Pageable pageable);
    Page<Payment> findByBooking_Id(Long bookingId, Pageable pageable);
    Page<Payment> findByStatus(String status, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT p FROM Payment p WHERE " +
           "(:bookingId IS NULL OR p.booking.id = :bookingId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:currency IS NULL OR p.currency = :currency) AND " +
           "(:method IS NULL OR p.method = :method) AND " +
           "(:minAmount IS NULL OR p.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR p.amount <= :maxAmount) AND " +
           "(:paidFrom IS NULL OR p.paidAt >= :paidFrom) AND " +
           "(:paidTo IS NULL OR p.paidAt <= :paidTo)")
    Page<Payment> findByFilters(@Param("bookingId") Long bookingId,
                                 @Param("status") String status,
                                 @Param("currency") String currency,
                                 @Param("method") String method,
                                 @Param("minAmount") BigDecimal minAmount,
                                 @Param("maxAmount") BigDecimal maxAmount,
                                 @Param("paidFrom") LocalDateTime paidFrom,
                                 @Param("paidTo") LocalDateTime paidTo,
                                 Pageable pageable);
}

