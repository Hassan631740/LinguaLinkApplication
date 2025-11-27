package com.lingualink.service;

import com.lingualink.entity.Review;
import com.lingualink.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Create
    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    // Read
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsByBookingId(Long bookingId) {
        return reviewRepository.findByBooking_Id(bookingId);
    }

    public List<Review> getReviewsByReviewerId(Long reviewerId) {
        return reviewRepository.findByReviewer_Id(reviewerId);
    }

    // Update
    public Review updateReview(Long id, Review reviewDetails) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        review.setBooking(reviewDetails.getBooking());
        review.setReviewer(reviewDetails.getReviewer());
        review.setRating(reviewDetails.getRating());
        review.setComment(reviewDetails.getComment());
        return reviewRepository.save(review);
    }

    // Delete
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}

