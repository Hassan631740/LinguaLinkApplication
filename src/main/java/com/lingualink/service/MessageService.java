package com.lingualink.service;

import com.lingualink.entity.Message;
import com.lingualink.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Create
    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    // Read
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public List<Message> getMessagesByBookingId(Long bookingId) {
        return messageRepository.findByBooking_Id(bookingId);
    }

    public List<Message> getMessagesBySenderAndReceiver(Long senderId, Long receiverId) {
        return messageRepository.findBySender_IdAndReceiver_Id(senderId, receiverId);
    }

    // Update
    public Message updateMessage(Long id, Message messageDetails) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        message.setBooking(messageDetails.getBooking());
        message.setSender(messageDetails.getSender());
        message.setReceiver(messageDetails.getReceiver());
        message.setContent(messageDetails.getContent());
        return messageRepository.save(message);
    }

    // Delete
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }
}

