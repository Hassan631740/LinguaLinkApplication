package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.BookingRequest;
import com.lingualink.dto.response.BookingResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Event;
import com.lingualink.entity.Interpreter;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.BookingMapper;
import com.lingualink.repository.BookingRepository;
import com.lingualink.repository.EventRepository;
import com.lingualink.repository.InterpreterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final EventRepository eventRepository;
    private final InterpreterRepository interpreterRepository;
    private final TransactionService transactionService;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper,
                         EventRepository eventRepository, InterpreterRepository interpreterRepository,
                         TransactionService transactionService) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.eventRepository = eventRepository;
        this.interpreterRepository = interpreterRepository;
        this.transactionService = transactionService;
    }

    // Create
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public BookingResponse createBooking(BookingRequest request) {
        Event event = request.getEventId() != null
                ? eventRepository.findById(request.getEventId())
                        .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()))
                : null;
        Interpreter interpreter = request.getInterpreterId() != null
                ? interpreterRepository.findById(request.getInterpreterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", request.getInterpreterId()))
                : null;
        Booking booking = bookingMapper.toEntity(request, event, interpreter);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Booking> getAllBookings(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Booking> getAllBookingsWithFilters(PageParams pageParams, Long eventId, Long interpreterId, 
                                                             String status, BigDecimal minPrice, BigDecimal maxPrice) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByFilters(eventId, interpreterId, status, minPrice, maxPrice, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookingsWithFiltersAsResponse(PageParams pageParams, Long eventId, Long interpreterId, 
                                                                              String status, BigDecimal minPrice, BigDecimal maxPrice) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByFilters(eventId, interpreterId, status, minPrice, maxPrice, pageable);
        List<BookingResponse> content = page.getContent().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsAsResponse() {
        List<Booking> bookings = getAllBookings();
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByIdAsResponse(Long id) {
        Booking booking = getBookingById(id);
        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByEventId(Long eventId) {
        return bookingRepository.findByEvent_Id(eventId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Booking> getBookingsByEventId(Long eventId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByEvent_Id(eventId, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getBookingsByEventIdAsResponse(Long eventId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByEvent_Id(eventId, pageable);
        List<BookingResponse> content = page.getContent().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEventIdAsResponse(Long eventId) {
        List<Booking> bookings = getBookingsByEventId(eventId);
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByInterpreterId(Long interpreterId) {
        return bookingRepository.findByInterpreter_Id(interpreterId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Booking> getBookingsByInterpreterId(Long interpreterId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByInterpreter_Id(interpreterId, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getBookingsByInterpreterIdAsResponse(Long interpreterId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByInterpreter_Id(interpreterId, pageable);
        List<BookingResponse> content = page.getContent().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByInterpreterIdAsResponse(Long interpreterId) {
        List<Booking> bookings = getBookingsByInterpreterId(interpreterId);
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Booking> getBookingsByStatus(String status, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByStatus(status, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getBookingsByStatusAsResponse(String status, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("requestedAt");
        Page<Booking> page = bookingRepository.findByStatus(status, pageable);
        List<BookingResponse> content = page.getContent().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByStatusAsResponse(String status) {
        List<Booking> bookings = getBookingsByStatus(status);
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Update - Full update
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking booking = getBookingById(id);
        booking.setEvent(bookingDetails.getEvent());
        booking.setInterpreter(bookingDetails.getInterpreter());
        booking.setStatus(bookingDetails.getStatus());
        booking.setConfirmedAt(bookingDetails.getConfirmedAt());
        booking.setPrice(bookingDetails.getPrice());
        booking.setPaymentId(bookingDetails.getPaymentId());
        return bookingRepository.save(booking);
    }

    public BookingResponse updateBooking(Long id, BookingRequest request) {
        Booking booking = getBookingById(id);
        Event event = request.getEventId() != null
                ? eventRepository.findById(request.getEventId())
                        .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()))
                : booking.getEvent();
        Interpreter interpreter = request.getInterpreterId() != null
                ? interpreterRepository.findById(request.getInterpreterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", request.getInterpreterId()))
                : booking.getInterpreter();
        bookingMapper.updateEntityFromRequest(request, booking, event, interpreter);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    // Update - Partial update
    public Booking patchBooking(Long id, Booking bookingDetails) {
        Booking booking = getBookingById(id);
        
        if (bookingDetails.getEvent() != null) {
            booking.setEvent(bookingDetails.getEvent());
        }
        if (bookingDetails.getInterpreter() != null) {
            booking.setInterpreter(bookingDetails.getInterpreter());
        }
        if (bookingDetails.getStatus() != null) {
            booking.setStatus(bookingDetails.getStatus());
        }
        if (bookingDetails.getConfirmedAt() != null) {
            booking.setConfirmedAt(bookingDetails.getConfirmedAt());
        }
        if (bookingDetails.getPrice() != null) {
            booking.setPrice(bookingDetails.getPrice());
        }
        if (bookingDetails.getPaymentId() != null) {
            booking.setPaymentId(bookingDetails.getPaymentId());
        }
        return bookingRepository.save(booking);
    }

    public BookingResponse patchBooking(Long id, BookingRequest request) {
        Booking booking = getBookingById(id);
        Event event = request.getEventId() != null
                ? eventRepository.findById(request.getEventId())
                        .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()))
                : null;
        Interpreter interpreter = request.getInterpreterId() != null
                ? interpreterRepository.findById(request.getInterpreterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Interpreter", "id", request.getInterpreterId()))
                : null;
        bookingMapper.updateEntityFromRequest(request, booking, event, interpreter);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    // Delete
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }

    /**
     * Complete a booking: deduct balance from client and record earnings for interpreter
     * This should be called after a call is completed
     */
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (booking.getPrice() == null || booking.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Booking price must be set and greater than zero");
        }

        if (booking.getStatus() != null && "COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already completed");
        }

        // Deduct balance from client
        transactionService.deductBalance(
                bookingId,
                booking.getPrice(),
                "USD" // Default currency, can be made configurable
        );

        // Record earnings for interpreter (80% of booking price, adjust as needed)
        BigDecimal interpreterEarning = booking.getPrice().multiply(new BigDecimal("0.80"));
        transactionService.recordEarning(
                bookingId,
                interpreterEarning,
                "USD"
        );

        // Update booking status
        booking.setStatus("COMPLETED");
        if (booking.getConfirmedAt() == null) {
            booking.setConfirmedAt(java.time.LocalDateTime.now());
        }
        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    /**
     * Complete a booking with custom duration (for calculating price based on actual call duration)
     * This is useful when the actual call duration differs from the booked duration
     */
    public BookingResponse completeBooking(Long bookingId, BigDecimal actualAmount, BigDecimal interpreterEarning) {
        Booking booking = getBookingById(bookingId);

        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Actual amount must be greater than zero");
        }

        if (interpreterEarning == null || interpreterEarning.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Interpreter earning must be greater than zero");
        }

        if (interpreterEarning.compareTo(actualAmount) > 0) {
            throw new IllegalArgumentException("Interpreter earning cannot exceed actual amount");
        }

        if (booking.getStatus() != null && "COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalArgumentException("Booking is already completed");
        }

        // Deduct balance from client
        transactionService.deductBalance(
                bookingId,
                actualAmount,
                "USD"
        );

        // Record earnings for interpreter
        transactionService.recordEarning(
                bookingId,
                interpreterEarning,
                "USD"
        );

        // Update booking status and price
        booking.setPrice(actualAmount);
        booking.setStatus("COMPLETED");
        if (booking.getConfirmedAt() == null) {
            booking.setConfirmedAt(java.time.LocalDateTime.now());
        }
        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }
}


