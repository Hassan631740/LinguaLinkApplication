package com.lingualink.controller;

import com.lingualink.entity.Review;
import com.lingualink.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Create
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review createdReview = reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    // Read - Get all
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // Read - Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(review -> ResponseEntity.ok(review))
                .orElse(ResponseEntity.notFound().build());
    }

    // Read - Get by booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Review>> getReviewsByBookingId(@PathVariable Long bookingId) {
        List<Review> reviews = reviewService.getReviewsByBookingId(bookingId);
        return ResponseEntity.ok(reviews);
    }

    // Read - Get by reviewer ID
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<Review>> getReviewsByReviewerId(@PathVariable Long reviewerId) {
        List<Review> reviews = reviewService.getReviewsByReviewerId(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review reviewDetails) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDetails);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

