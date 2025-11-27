package com.lingualink.service;

import com.lingualink.entity.Booking;
import com.lingualink.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByEventId(Long eventId) {
        return bookingRepository.findByEvent_Id(eventId);
    }

    public List<Booking> getBookingsByInterpreterId(Long interpreterId) {
        return bookingRepository.findByInterpreter_Id(interpreterId);
    }

    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    // Update
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        booking.setEvent(bookingDetails.getEvent());
        booking.setInterpreter(bookingDetails.getInterpreter());
        booking.setStatus(bookingDetails.getStatus());
        booking.setConfirmedAt(bookingDetails.getConfirmedAt());
        booking.setPrice(bookingDetails.getPrice());
        booking.setPaymentId(bookingDetails.getPaymentId());
        return bookingRepository.save(booking);
    }

    // Delete
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
}

