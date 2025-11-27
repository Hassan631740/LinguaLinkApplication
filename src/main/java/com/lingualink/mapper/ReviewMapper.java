package com.lingualink.mapper;

import com.lingualink.dto.request.ReviewRequest;
import com.lingualink.dto.response.ReviewResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Review;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequest request, Booking booking, User reviewer) {
        if (request == null) {
            return null;
        }
        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(reviewer);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return review;
    }

    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setBookingId(review.getBooking() != null ? review.getBooking().getId() : null);
        response.setReviewerId(review.getReviewer() != null ? review.getReviewer().getId() : null);
        response.setReviewerName(review.getReviewer() != null ? review.getReviewer().getName() : null);
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(ReviewRequest request, Review review, Booking booking, User reviewer) {
        if (request == null || review == null) {
            return;
        }
        if (booking != null) {
            review.setBooking(booking);
        }
        if (reviewer != null) {
            review.setReviewer(reviewer);
        }
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
    }
}

