package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.BookingRequest;
import com.lingualink.dto.response.BookingResponse;
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
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse createdBooking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/bookings/" + createdBooking.getId())
                .body(createdBooking);
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
            PagedResponse<BookingResponse> pagedResponse = bookingService.getAllBookingsWithFiltersAsResponse(
                    pageParams, eventId, interpreterId, status, minPrice, maxPrice);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<BookingResponse> bookings = bookingService.getAllBookingsAsResponse();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieves a booking by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingResponse> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingByIdAsResponse(id);
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
            PagedResponse<BookingResponse> pagedResponse = bookingService.getBookingsByEventIdAsResponse(eventId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<BookingResponse> bookings = bookingService.getBookingsByEventIdAsResponse(eventId);
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
            PagedResponse<BookingResponse> pagedResponse = bookingService.getBookingsByInterpreterIdAsResponse(interpreterId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<BookingResponse> bookings = bookingService.getBookingsByInterpreterIdAsResponse(interpreterId);
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
            PagedResponse<BookingResponse> pagedResponse = bookingService.getBookingsByStatusAsResponse(status, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<BookingResponse> bookings = bookingService.getBookingsByStatusAsResponse(status);
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
    public ResponseEntity<BookingResponse> updateBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse updatedBooking = bookingService.updateBooking(id, request);
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
    public ResponseEntity<BookingResponse> patchBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse updatedBooking = bookingService.patchBooking(id, request);
        return ResponseEntity.ok(updatedBooking);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Complete booking", description = "Complete a booking after call ends. Deducts balance from client and records earnings for interpreter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid booking state or insufficient balance"),
            @ApiResponse(responseCode = "402", description = "Insufficient balance"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingResponse> completeBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        BookingResponse completedBooking = bookingService.completeBooking(id);
        return ResponseEntity.ok(completedBooking);
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
