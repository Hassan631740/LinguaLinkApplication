package com.lingualink.repository;

import com.lingualink.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByEvent_Id(Long eventId);
    List<Booking> findByInterpreter_Id(Long interpreterId);
    List<Booking> findByStatus(String status);
    
    // Pagination support
    Page<Booking> findAll(Pageable pageable);
    Page<Booking> findByEvent_Id(Long eventId, Pageable pageable);
    Page<Booking> findByInterpreter_Id(Long interpreterId, Pageable pageable);
    Page<Booking> findByStatus(String status, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT b FROM Booking b WHERE " +
           "(:eventId IS NULL OR b.event.id = :eventId) AND " +
           "(:interpreterId IS NULL OR b.interpreter.id = :interpreterId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR b.price <= :maxPrice)")
    Page<Booking> findByFilters(@Param("eventId") Long eventId,
                                 @Param("interpreterId") Long interpreterId,
                                 @Param("status") String status,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);
}

