package com.lingualink.service;

import com.lingualink.entity.Booking;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Create
    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByEventId(Long eventId) {
        return bookingRepository.findByEvent_Id(eventId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByInterpreterId(Long interpreterId) {
        return bookingRepository.findByInterpreter_Id(interpreterId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
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

    // Delete
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }
}


