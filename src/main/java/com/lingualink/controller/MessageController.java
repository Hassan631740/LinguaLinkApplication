package com.lingualink.controller;

import com.lingualink.dto.PagedResponse;
import com.lingualink.entity.Message;
import com.lingualink.service.MessageService;
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
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Message management API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Create a new message", description = "Creates a new message in the system (All authenticated users)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Message> createMessage(@Valid @RequestBody Message message) {
        Message createdMessage = messageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @GetMapping
    @Operation(summary = "Get all messages", description = "Retrieves a paginated list of all messages with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of messages")
    })
    public ResponseEntity<?> getAllMessages(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir,
            @Parameter(description = "Filter by booking ID") @RequestParam(required = false) Long bookingId,
            @Parameter(description = "Filter by sender ID") @RequestParam(required = false) Long senderId,
            @Parameter(description = "Filter by receiver ID") @RequestParam(required = false) Long receiverId,
            @Parameter(description = "Filter by content (partial match)") @RequestParam(required = false) String content,
            @Parameter(description = "Filter by sent date from (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sentFrom,
            @Parameter(description = "Filter by sent date to (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime sentTo) {
        
        if (page != null || size != null || bookingId != null || senderId != null || 
            receiverId != null || content != null || sentFrom != null || sentTo != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Message> pagedResponse = messageService.getAllMessagesWithFilters(
                    pageParams, bookingId, senderId, receiverId, content, sentFrom, sentTo);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Message> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Retrieves a message by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message found"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<Message> getMessageById(
            @Parameter(description = "Message ID") @PathVariable Long id) {
        Message message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get messages by booking ID", description = "Retrieves all messages for a specific booking with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    public ResponseEntity<?> getMessagesByBookingId(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Message> pagedResponse = messageService.getMessagesByBookingId(bookingId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Message> messages = messageService.getMessagesByBookingId(bookingId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/sender/{senderId}/receiver/{receiverId}")
    @Operation(summary = "Get messages between users", description = "Retrieves all messages between a sender and receiver with optional pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    public ResponseEntity<?> getMessagesBySenderAndReceiver(
            @Parameter(description = "Sender user ID") @PathVariable Long senderId,
            @Parameter(description = "Receiver user ID") @PathVariable Long receiverId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (1-100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(required = false) String sortDir) {
        
        if (page != null || size != null) {
            var pageParams = PaginationUtil.parsePageParams(page, size, sortBy, sortDir);
            PagedResponse<Message> pagedResponse = messageService.getMessagesBySenderAndReceiver(senderId, receiverId, pageParams);
            return ResponseEntity.ok(pagedResponse);
        }
        
        List<Message> messages = messageService.getMessagesBySenderAndReceiver(senderId, receiverId);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Update message", description = "Fully updates an existing message. Users can only update their own messages unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message updated successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Message> updateMessage(
            @Parameter(description = "Message ID") @PathVariable Long id,
            @Valid @RequestBody Message messageDetails) {
        Message updatedMessage = messageService.updateMessage(id, messageDetails);
        return ResponseEntity.ok(updatedMessage);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Partially update message", description = "Partially updates an existing message. Users can only update their own messages unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message updated successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Message> patchMessage(
            @Parameter(description = "Message ID") @PathVariable Long id,
            @RequestBody Message messageDetails) {
        Message updatedMessage = messageService.patchMessage(id, messageDetails);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'INTERPRETER', 'ADMINISTRATOR')")
    @Operation(summary = "Delete message", description = "Deletes a message by its ID. Users can only delete their own messages unless they are administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "Message ID") @PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
