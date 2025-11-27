package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.entity.Booking;
import com.lingualink.service.BookingService;
import com.lingualink.util.PaginationUtil;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Booking management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new booking", description = "Creates a new booking in the system (Client or Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking booking) {
        Booking createdBooking = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Retrieves a paginated list of all bookings with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of bookings")
    })
    public ResponseEntity<?> getAllBookings(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by event ID") @RequestParam(required = false) Long eventId,
            @Parameter(description = "Filter by interpreter ID") @RequestParam(required = false) Long interpreterId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Filter by maximum price") @RequestParam(required = false) BigDecimal maxPrice) {
        
        if (page != null || size != null || eventId != null || interpreterId != null || 
            status != null || minPrice != null || maxPrice != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Booking> pagedResponse = bookingService.getAllBookingsWithFilters(
                    pageParams, eventId, interpreterId, status, minPrice, maxPrice);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieves a booking by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Booking> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get bookings by event ID", description = "Retrieves all bookings for a specific event with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bookings")
    public ResponseEntity<?> getBookingsByEventId(
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Booking> pagedResponse = bookingService.getBookingsByEventId(eventId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Booking> bookings = bookingService.getBookingsByEventId(eventId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/interpreter/{interpreterId}")
    @Operation(summary = "Get bookings by interpreter ID", description = "Retrieves all bookings for a specific interpreter with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bookings")
    public ResponseEntity<?> getBookingsByInterpreterId(
            @Parameter(description = "Interpreter ID") @PathVariable Long interpreterId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Booking> pagedResponse = bookingService.getBookingsByInterpreterId(interpreterId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Booking> bookings = bookingService.getBookingsByInterpreterId(interpreterId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get bookings by status", description = "Retrieves all bookings with a specific status with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved bookings")
    public ResponseEntity<?> getBookingsByStatus(
            @Parameter(description = "Booking status") @PathVariable String status,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Booking> pagedResponse = bookingService.getBookingsByStatus(status, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Update booking", description = "Fully updates an existing booking. Clients can update their own bookings, interpreters can update assigned bookings, administrators can update any booking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Booking> updateBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Valid @RequestBody Booking bookingDetails) {
        Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Partially update booking", description = "Partially updates an existing booking. Clients can update their own bookings, interpreters can update assigned bookings, administrators can update any booking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Booking> patchBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @RequestBody Booking bookingDetails) {
        Booking updatedBooking = bookingService.patchBooking(id, bookingDetails);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Delete booking", description = "Deletes a booking by its ID. Clients can delete their own bookings, administrators can delete any booking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
