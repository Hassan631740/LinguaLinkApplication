package com.lingualink.controller;

import com.lingualink.entity.Booking;
import com.lingualink.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking createdBooking = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    // Read - Get all
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Read - Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(booking -> ResponseEntity.ok(booking))
                .orElse(ResponseEntity.notFound().build());
    }

    // Read - Get by event ID
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Booking>> getBookingsByEventId(@PathVariable Long eventId) {
        List<Booking> bookings = bookingService.getBookingsByEventId(eventId);
        return ResponseEntity.ok(bookings);
    }

    // Read - Get by interpreter ID
    @GetMapping("/interpreter/{interpreterId}")
    public ResponseEntity<List<Booking>> getBookingsByInterpreterId(@PathVariable Long interpreterId) {
        List<Booking> bookings = bookingService.getBookingsByInterpreterId(interpreterId);
        return ResponseEntity.ok(bookings);
    }

    // Read - Get by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable String status) {
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking bookingDetails) {
        try {
            Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

