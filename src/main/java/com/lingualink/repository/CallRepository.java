package com.lingualink.repository;

import com.lingualink.entity.Call;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
    List<Call> findByBooking_Id(Long bookingId);
    List<Call> findByClient_Id(Long clientId);
    List<Call> findByInterpreter_Id(Long interpreterId);
    List<Call> findByStatus(String status);
    
    // Pagination support
    Page<Call> findAll(Pageable pageable);
    Page<Call> findByBooking_Id(Long bookingId, Pageable pageable);
    Page<Call> findByClient_Id(Long clientId, Pageable pageable);
    Page<Call> findByInterpreter_Id(Long interpreterId, Pageable pageable);
    Page<Call> findByStatus(String status, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT c FROM Call c WHERE " +
           "(:bookingId IS NULL OR c.booking.id = :bookingId) AND " +
           "(:clientId IS NULL OR c.client.id = :clientId) AND " +
           "(:interpreterId IS NULL OR c.interpreter.id = :interpreterId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:startTimeFrom IS NULL OR c.startTime >= :startTimeFrom) AND " +
           "(:startTimeTo IS NULL OR c.startTime <= :startTimeTo)")
    Page<Call> findByFilters(@Param("bookingId") Long bookingId,
                              @Param("clientId") Long clientId,
                              @Param("interpreterId") Long interpreterId,
                              @Param("status") String status,
                              @Param("startTimeFrom") LocalDateTime startTimeFrom,
                              @Param("startTimeTo") LocalDateTime startTimeTo,
                              Pageable pageable);
    
    List<Call> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}

