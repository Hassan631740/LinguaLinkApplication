package com.lingualink.repository;

import com.lingualink.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBooking_Id(Long bookingId);
    List<Review> findByReviewer_Id(Long reviewerId);
    
    // Pagination support
    Page<Review> findAll(Pageable pageable);
    Page<Review> findByBooking_Id(Long bookingId, Pageable pageable);
    Page<Review> findByReviewer_Id(Long reviewerId, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT r FROM Review r WHERE " +
           "(:bookingId IS NULL OR r.booking.id = :bookingId) AND " +
           "(:reviewerId IS NULL OR r.reviewer.id = :reviewerId) AND " +
           "(:minRating IS NULL OR r.rating >= :minRating) AND " +
           "(:maxRating IS NULL OR r.rating <= :maxRating) AND " +
           "(:comment IS NULL OR LOWER(r.comment) LIKE LOWER(CONCAT('%', :comment, '%')))")
    Page<Review> findByFilters(@Param("bookingId") Long bookingId,
                               @Param("reviewerId") Long reviewerId,
                               @Param("minRating") Integer minRating,
                               @Param("maxRating") Integer maxRating,
                               @Param("comment") String comment,
                               Pageable pageable);
}

