package com.lingualink.controller;

import com.lingualink.entity.Review;
import com.lingualink.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Review management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new review", description = "Creates a new review in the system (All authenticated users)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        Review createdReview = reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping
    @Operation(summary = "Get all reviews", description = "Retrieves a list of all reviews")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of reviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieves a review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review found"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<Review> getReviewById(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get reviews by booking ID", description = "Retrieves all reviews for a specific booking")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews")
    public ResponseEntity<List<Review>> getReviewsByBookingId(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId) {
        List<Review> reviews = reviewService.getReviewsByBookingId(bookingId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviewer/{reviewerId}")
    @Operation(summary = "Get reviews by reviewer ID", description = "Retrieves all reviews written by a specific reviewer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews")
    public ResponseEntity<List<Review>> getReviewsByReviewerId(
            @Parameter(description = "Reviewer user ID") @PathVariable Long reviewerId) {
        List<Review> reviews = reviewService.getReviewsByReviewerId(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Update review", description = "Fully updates an existing review. Users can only update their own reviews unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Review> updateReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @Valid @RequestBody Review reviewDetails) {
        Review updatedReview = reviewService.updateReview(id, reviewDetails);
        return ResponseEntity.ok(updatedReview);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Partially update review", description = "Partially updates an existing review. Users can only update their own reviews unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Review> patchReview(
            @Parameter(description = "Review ID") @PathVariable Long id,
            @RequestBody Review reviewDetails) {
        Review updatedReview = reviewService.patchReview(id, reviewDetails);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Delete review", description = "Deletes a review by its ID. Users can only delete their own reviews unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
