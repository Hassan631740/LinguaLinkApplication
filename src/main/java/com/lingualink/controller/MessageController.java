package com.lingualink.controller;

import com.lingualink.entity.Message;
import com.lingualink.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // Create
    @PostMapping
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {
        Message createdMessage = messageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    // Read - Get all
    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages() {
        List<Message> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    // Read - Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(message -> ResponseEntity.ok(message))
                .orElse(ResponseEntity.notFound().build());
    }

    // Read - Get by booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Message>> getMessagesByBookingId(@PathVariable Long bookingId) {
        List<Message> messages = messageService.getMessagesByBookingId(bookingId);
        return ResponseEntity.ok(messages);
    }

    // Read - Get by sender and receiver
    @GetMapping("/sender/{senderId}/receiver/{receiverId}")
    public ResponseEntity<List<Message>> getMessagesBySenderAndReceiver(
            @PathVariable Long senderId,
            @PathVariable Long receiverId) {
        List<Message> messages = messageService.getMessagesBySenderAndReceiver(senderId, receiverId);
        return ResponseEntity.ok(messages);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable Long id, @RequestBody Message messageDetails) {
        try {
            Message updatedMessage = messageService.updateMessage(id, messageDetails);
            return ResponseEntity.ok(updatedMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

