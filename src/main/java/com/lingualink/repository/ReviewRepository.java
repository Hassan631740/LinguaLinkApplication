package com.lingualink.repository;

import com.lingualink.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBooking_Id(Long bookingId);
    List<Review> findByReviewer_Id(Long reviewerId);
}

