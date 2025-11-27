package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.entity.Review;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Create
    public Review createReview(Review review) {
        validateRating(review.getRating());
        return reviewRepository.save(review);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Review> getAllReviews(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Review> getAllReviewsWithFilters(PageParams pageParams, Long bookingId, Long reviewerId, 
                                                           Integer minRating, Integer maxRating, String comment) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findByFilters(bookingId, reviewerId, minRating, maxRating, comment, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByBookingId(Long bookingId) {
        return reviewRepository.findByBooking_Id(bookingId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Review> getReviewsByBookingId(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findByBooking_Id(bookingId, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByReviewerId(Long reviewerId) {
        return reviewRepository.findByReviewer_Id(reviewerId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Review> getReviewsByReviewerId(Long reviewerId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findByReviewer_Id(reviewerId, pageable);
        return PagedResponse.of(page);
    }

    // Update - Full update
    public Review updateReview(Long id, Review reviewDetails) {
        Review review = getReviewById(id);
        validateRating(reviewDetails.getRating());
        review.setBooking(reviewDetails.getBooking());
        review.setReviewer(reviewDetails.getReviewer());
        review.setRating(reviewDetails.getRating());
        review.setComment(reviewDetails.getComment());
        return reviewRepository.save(review);
    }

    // Update - Partial update
    public Review patchReview(Long id, Review reviewDetails) {
        Review review = getReviewById(id);
        
        if (reviewDetails.getBooking() != null) {
            review.setBooking(reviewDetails.getBooking());
        }
        if (reviewDetails.getReviewer() != null) {
            review.setReviewer(reviewDetails.getReviewer());
        }
        if (reviewDetails.getRating() != null) {
            validateRating(reviewDetails.getRating());
            review.setRating(reviewDetails.getRating());
        }
        if (reviewDetails.getComment() != null) {
            review.setComment(reviewDetails.getComment());
        }
        return reviewRepository.save(review);
    }

    // Delete
    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        reviewRepository.delete(review);
    }

    private void validateRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}


