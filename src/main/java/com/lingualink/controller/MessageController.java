package com.lingualink.controller;

import com.lingualink.entity.Message;
import com.lingualink.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Create a new message", description = "Creates a new message in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Message> createMessage(@Valid @RequestBody Message message) {
        Message createdMessage = messageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @GetMapping
    @Operation(summary = "Get all messages", description = "Retrieves a list of all messages")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of messages")
    public ResponseEntity<List<Message>> getAllMessages() {
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
    @Operation(summary = "Get messages by booking ID", description = "Retrieves all messages for a specific booking")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    public ResponseEntity<List<Message>> getMessagesByBookingId(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId) {
        List<Message> messages = messageService.getMessagesByBookingId(bookingId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/sender/{senderId}/receiver/{receiverId}")
    @Operation(summary = "Get messages between users", description = "Retrieves all messages between a sender and receiver")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved messages")
    public ResponseEntity<List<Message>> getMessagesBySenderAndReceiver(
            @Parameter(description = "Sender user ID") @PathVariable Long senderId,
            @Parameter(description = "Receiver user ID") @PathVariable Long receiverId) {
        List<Message> messages = messageService.getMessagesBySenderAndReceiver(senderId, receiverId);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update message", description = "Fully updates an existing message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message updated successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Message> updateMessage(
            @Parameter(description = "Message ID") @PathVariable Long id,
            @Valid @RequestBody Message messageDetails) {
        Message updatedMessage = messageService.updateMessage(id, messageDetails);
        return ResponseEntity.ok(updatedMessage);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update message", description = "Partially updates an existing message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message updated successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Message> patchMessage(
            @Parameter(description = "Message ID") @PathVariable Long id,
            @RequestBody Message messageDetails) {
        Message updatedMessage = messageService.patchMessage(id, messageDetails);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message", description = "Deletes a message by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "Message ID") @PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
