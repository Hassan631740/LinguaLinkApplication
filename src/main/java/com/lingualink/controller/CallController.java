package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.CallRequest;
import com.lingualink.dto.response.CallResponse;
import com.lingualink.service.CallService;
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
@RequestMapping("/api/calls")
@Tag(name = "Calls", description = "Call records management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CallController {
    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new call record", description = "Creates a new call record in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Call record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CallResponse> createCall(@Valid @RequestBody CallRequest request) {
        CallResponse createdCall = callService.createCall(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/calls/" + createdCall.getId())
                .body(createdCall);
    }

    // READ
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Get all call records", description = "Retrieves a paginated list of call records with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of call records")
    })
    public ResponseEntity<?> getAllCalls(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) Long bookingId,
            @Parameter(description = "Filter by client ID") @RequestParam(required = false) Long clientId,
            @Parameter(description = "Filter by interpreter ID") @RequestParam(required = false) Long interpreterId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by start time from (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTimeFrom,
            @Parameter(description = "Filter by start time to (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTimeTo) {
        
        if (page != null || size != null || bookingId != null || clientId != null || 
            interpreterId != null || status != null || startTimeFrom != null || startTimeTo != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<CallResponse> pagedResponse = callService.getAllCallsWithFiltersAsResponse(
                    pageParams, bookingId, clientId, interpreterId, status, startTimeFrom, startTimeTo);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<CallResponse> calls = callService.getAllCallsAsResponse();
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Get call record by ID", description = "Retrieves a call record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call record found"),
            @ApiResponse(responseCode = "404", description = "Call record not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CallResponse> getCallById(
            @Parameter(description = "Call record ID") @PathVariable Long id) {
        CallResponse call = callService.getCallByIdAsResponse(id);
        return ResponseEntity.ok(call);
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Get call records by booking ID", description = "Retrieves all call records for a specific booking")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved call records")
    public ResponseEntity<List<CallResponse>> getCallsByBookingId(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId) {
        List<CallResponse> calls = callService.getCallsByBookingIdAsResponse(bookingId);
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Get call records by client ID", description = "Retrieves all call records for a specific client")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved call records")
    public ResponseEntity<List<CallResponse>> getCallsByClientId(
            @Parameter(description = "Client user ID") @PathVariable Long clientId) {
        List<CallResponse> calls = callService.getCallsByClientIdAsResponse(clientId);
        return ResponseEntity.ok(calls);
    }

    @GetMapping("/interpreter/{interpreterId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Get call records by interpreter ID", description = "Retrieves all call records for a specific interpreter")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved call records")
    public ResponseEntity<List<CallResponse>> getCallsByInterpreterId(
            @Parameter(description = "Interpreter ID") @PathVariable Long interpreterId) {
        List<CallResponse> calls = callService.getCallsByInterpreterIdAsResponse(interpreterId);
        return ResponseEntity.ok(calls);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Update call record", description = "Fully updates an existing call record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call record updated successfully"),
            @ApiResponse(responseCode = "404", description = "Call record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CallResponse> updateCall(
            @Parameter(description = "Call record ID") @PathVariable Long id,
            @Valid @RequestBody CallRequest request) {
        CallResponse updatedCall = callService.updateCall(id, request);
        return ResponseEntity.ok(updatedCall);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Partially update call record", description = "Partially updates an existing call record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Call record updated successfully"),
            @ApiResponse(responseCode = "404", description = "Call record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<CallResponse> patchCall(
            @Parameter(description = "Call record ID") @PathVariable Long id,
            @Valid @RequestBody CallRequest request) {
        CallResponse updatedCall = callService.patchCall(id, request);
        return ResponseEntity.ok(updatedCall);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Delete call record", description = "Deletes a call record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Call record deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Call record not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteCall(
            @Parameter(description = "Call record ID") @PathVariable Long id) {
        callService.deleteCall(id);
        return ResponseEntity.noContent().build();
    }
}
