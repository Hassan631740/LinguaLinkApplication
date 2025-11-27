package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.entity.Event;
import com.lingualink.service.EventService;
import com.lingualink.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new event", description = "Creates a new event in the system (Client or Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Retrieves a paginated list of all events with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of events")
    })
    public ResponseEntity<?> getAllEvents(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by organizer ID") @RequestParam(required = false) Long organizerId,
            @Parameter(description = "Filter by language") @RequestParam(required = false) String language,
            @Parameter(description = "Filter by title (partial match)") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by location (partial match)") @RequestParam(required = false) String location,
            @Parameter(description = "Filter by start date from (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateFrom,
            @Parameter(description = "Filter by start date to (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTo) {
        
        // If pagination or filter parameters are provided, return paginated response
        if (page != null || size != null || organizerId != null || language != null || 
            title != null || location != null || startDateFrom != null || startDateTo != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Event> pagedResponse = eventService.getAllEventsWithFilters(
                    pageParams, organizerId, language, title, location, startDateFrom, startDateTo);
            return ResponseEntity.ok(pagedResponse);
        }
        
        // Otherwise, return simple list for backward compatibility
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves an event by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event found"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Event> getEventById(
            @Parameter(description = "Event ID") @PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/organizer/{organizerId}")
    @Operation(summary = "Get events by organizer", description = "Retrieves all events organized by a specific user with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<?> getEventsByOrganizer(
            @Parameter(description = "Organizer user ID") @PathVariable Long organizerId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Event> pagedResponse = eventService.getEventsByOrganizerId(organizerId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Event> events = eventService.getEventsByOrganizerId(organizerId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/language/{language}")
    @Operation(summary = "Get events by language", description = "Retrieves all events for a specific language with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved events")
    public ResponseEntity<?> getEventsByLanguage(
            @Parameter(description = "Language code") @PathVariable String language,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Event> pagedResponse = eventService.getEventsByLanguage(language, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Event> events = eventService.getEventsByLanguage(language);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Update event", description = "Fully updates an existing event. Only the event organizer or administrators can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Event> updateEvent(
            @Parameter(description = "Event ID") @PathVariable Long id,
            @Valid @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(id, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Partially update event", description = "Partially updates an existing event. Only the event organizer or administrators can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Event> patchEvent(
            @Parameter(description = "Event ID") @PathVariable Long id,
            @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.patchEvent(id, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMINISTRATOR')")
    @Operation(summary = "Delete event", description = "Deletes an event by its ID. Only the event organizer or administrators can delete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
