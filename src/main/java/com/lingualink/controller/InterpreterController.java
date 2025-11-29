package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.InterpreterRequest;
import com.lingualink.dto.response.InterpreterResponse;
import com.lingualink.service.InterpreterService;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/interpreters")
@Tag(name = "Interpreters", description = "Interpreter management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InterpreterController {
    private final InterpreterService interpreterService;

    public InterpreterController(InterpreterService interpreterService) {
        this.interpreterService = interpreterService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new interpreter", description = "Creates a new interpreter profile in the system (Interpreter or Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Interpreter created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<InterpreterResponse> createInterpreter(@Valid @RequestBody InterpreterRequest request) {
        InterpreterResponse createdInterpreter = interpreterService.createInterpreter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/interpreters/" + createdInterpreter.getId())
                .body(createdInterpreter);
    }

    @GetMapping
    @Operation(summary = "Get all interpreters", description = "Retrieves a paginated list of all interpreters with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of interpreters")
    })
    public ResponseEntity<?> getAllInterpreters(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by language") @RequestParam(required = false) String language,
            @Parameter(description = "Filter by minimum rate per hour") @RequestParam(required = false) BigDecimal minRate,
            @Parameter(description = "Filter by maximum rate per hour") @RequestParam(required = false) BigDecimal maxRate,
            @Parameter(description = "Filter by minimum experience years") @RequestParam(required = false) Integer minExperience,
            @Parameter(description = "Filter by maximum experience years") @RequestParam(required = false) Integer maxExperience) {
        
        if (page != null || size != null || language != null || minRate != null || 
            maxRate != null || minExperience != null || maxExperience != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<InterpreterResponse> pagedResponse = interpreterService.getAllInterpretersWithFiltersAsResponse(
                    pageParams, language, minRate, maxRate, minExperience, maxExperience);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<InterpreterResponse> interpreters = interpreterService.getAllInterpretersAsResponse();
        return ResponseEntity.ok(interpreters);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interpreter by ID", description = "Retrieves an interpreter by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interpreter found"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found")
    })
    public ResponseEntity<InterpreterResponse> getInterpreterById(
            @Parameter(description = "Interpreter ID") @PathVariable Long id) {
        InterpreterResponse interpreter = interpreterService.getInterpreterByIdAsResponse(id);
        return ResponseEntity.ok(interpreter);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get interpreter by user ID", description = "Retrieves an interpreter profile by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interpreter found"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found")
    })
    public ResponseEntity<InterpreterResponse> getInterpreterByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        Optional<InterpreterResponse> interpreter = interpreterService.getInterpreterByUserIdAsResponse(userId);
        return interpreter.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Update interpreter", description = "Fully updates an existing interpreter profile. Interpreters can update their own profile, administrators can update any profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interpreter updated successfully"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<InterpreterResponse> updateInterpreter(
            @Parameter(description = "Interpreter ID") @PathVariable Long id,
            @Valid @RequestBody InterpreterRequest request) {
        InterpreterResponse updatedInterpreter = interpreterService.updateInterpreter(id, request);
        return ResponseEntity.ok(updatedInterpreter);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Partially update interpreter", description = "Partially updates an existing interpreter profile. Interpreters can update their own profile, administrators can update any profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interpreter updated successfully"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<InterpreterResponse> patchInterpreter(
            @Parameter(description = "Interpreter ID") @PathVariable Long id,
            @Valid @RequestBody InterpreterRequest request) {
        InterpreterResponse updatedInterpreter = interpreterService.patchInterpreter(id, request);
        return ResponseEntity.ok(updatedInterpreter);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete interpreter", description = "Deletes an interpreter profile by its ID (Administrator only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Interpreter deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteInterpreter(
            @Parameter(description = "Interpreter ID") @PathVariable Long id) {
        interpreterService.deleteInterpreter(id);
        return ResponseEntity.noContent().build();
    }
}
