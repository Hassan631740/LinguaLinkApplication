package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.ReviewRequest;
import com.lingualink.dto.response.ReviewResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Review;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.ReviewMapper;
import com.lingualink.repository.BookingRepository;
import com.lingualink.repository.ReviewRepository;
import com.lingualink.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ReviewMapper reviewMapper,
                        BookingRepository bookingRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Create
    public Review createReview(Review review) {
        validateRating(review.getRating());
        return reviewRepository.save(review);
    }

    public ReviewResponse createReview(ReviewRequest request) {
        validateRating(request.getRating());
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        User reviewer = request.getReviewerId() != null
                ? userRepository.findById(request.getReviewerId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReviewerId()))
                : null;
        Review review = reviewMapper.toEntity(request, booking, reviewer);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
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
    public PagedResponse<ReviewResponse> getAllReviewsWithFiltersAsResponse(PageParams pageParams, Long bookingId, Long reviewerId, 
                                                                           Integer minRating, Integer maxRating, String comment) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findByFilters(bookingId, reviewerId, minRating, maxRating, comment, pageable);
        List<ReviewResponse> content = page.getContent().stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviewsAsResponse() {
        List<Review> reviews = getAllReviews();
        return reviews.stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByIdAsResponse(Long id) {
        Review review = getReviewById(id);
        return reviewMapper.toResponse(review);
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
    public PagedResponse<ReviewResponse> getReviewsByBookingIdAsResponse(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findByBooking_Id(bookingId, pageable);
        List<ReviewResponse> content = page.getContent().stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByBookingIdAsResponse(Long bookingId) {
        List<Review> reviews = getReviewsByBookingId(bookingId);
        return reviews.stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getReviewsByReviewerIdAsResponse(Long reviewerId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("createdAt");
        Page<Review> page = reviewRepository.findByReviewer_Id(reviewerId, pageable);
        List<ReviewResponse> content = page.getContent().stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByReviewerIdAsResponse(Long reviewerId) {
        List<Review> reviews = getReviewsByReviewerId(reviewerId);
        return reviews.stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
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

    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = getReviewById(id);
        validateRating(request.getRating());
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : review.getBooking();
        User reviewer = request.getReviewerId() != null
                ? userRepository.findById(request.getReviewerId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReviewerId()))
                : review.getReviewer();
        reviewMapper.updateEntityFromRequest(request, review, booking, reviewer);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
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

    public ReviewResponse patchReview(Long id, ReviewRequest request) {
        Review review = getReviewById(id);
        if (request.getRating() != null) {
            validateRating(request.getRating());
        }
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        User reviewer = request.getReviewerId() != null
                ? userRepository.findById(request.getReviewerId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReviewerId()))
                : null;
        reviewMapper.updateEntityFromRequest(request, review, booking, reviewer);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
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


