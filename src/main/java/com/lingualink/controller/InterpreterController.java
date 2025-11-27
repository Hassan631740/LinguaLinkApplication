package com.lingualink.controller;

import com.lingualink.entity.Interpreter;
import com.lingualink.service.InterpreterService;
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
    public ResponseEntity<Interpreter> createInterpreter(@Valid @RequestBody Interpreter interpreter) {
        Interpreter createdInterpreter = interpreterService.createInterpreter(interpreter);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInterpreter);
    }

    @GetMapping
    @Operation(summary = "Get all interpreters", description = "Retrieves a list of all interpreters")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of interpreters")
    public ResponseEntity<List<Interpreter>> getAllInterpreters() {
        List<Interpreter> interpreters = interpreterService.getAllInterpreters();
        return ResponseEntity.ok(interpreters);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interpreter by ID", description = "Retrieves an interpreter by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interpreter found"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found")
    })
    public ResponseEntity<Interpreter> getInterpreterById(
            @Parameter(description = "Interpreter ID") @PathVariable Long id) {
        Interpreter interpreter = interpreterService.getInterpreterById(id);
        return ResponseEntity.ok(interpreter);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get interpreter by user ID", description = "Retrieves an interpreter profile by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interpreter found"),
            @ApiResponse(responseCode = "404", description = "Interpreter not found")
    })
    public ResponseEntity<Interpreter> getInterpreterByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        Optional<Interpreter> interpreter = interpreterService.getInterpreterByUserId(userId);
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
    public ResponseEntity<Interpreter> updateInterpreter(
            @Parameter(description = "Interpreter ID") @PathVariable Long id,
            @Valid @RequestBody Interpreter interpreterDetails) {
        Interpreter updatedInterpreter = interpreterService.updateInterpreter(id, interpreterDetails);
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
    public ResponseEntity<Interpreter> patchInterpreter(
            @Parameter(description = "Interpreter ID") @PathVariable Long id,
            @RequestBody Interpreter interpreterDetails) {
        Interpreter updatedInterpreter = interpreterService.patchInterpreter(id, interpreterDetails);
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
