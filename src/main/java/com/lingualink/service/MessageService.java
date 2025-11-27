package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.entity.Message;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
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
    @Transactional(readOnly = true)
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Message> getAllMessages(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Message> getAllMessagesWithFilters(PageParams pageParams, Long bookingId, Long senderId, 
                                                             Long receiverId, String content,
                                                             LocalDateTime sentFrom, LocalDateTime sentTo) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findByFilters(bookingId, senderId, receiverId, content, 
                                                              sentFrom, sentTo, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesByBookingId(Long bookingId) {
        return messageRepository.findByBooking_Id(bookingId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Message> getMessagesByBookingId(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findByBooking_Id(bookingId, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesBySenderAndReceiver(Long senderId, Long receiverId) {
        return messageRepository.findBySender_IdAndReceiver_Id(senderId, receiverId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Message> getMessagesBySenderAndReceiver(Long senderId, Long receiverId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findBySender_IdAndReceiver_Id(senderId, receiverId, pageable);
        return PagedResponse.of(page);
    }

    // Update - Full update
    public Message updateMessage(Long id, Message messageDetails) {
        Message message = getMessageById(id);
        message.setBooking(messageDetails.getBooking());
        message.setSender(messageDetails.getSender());
        message.setReceiver(messageDetails.getReceiver());
        message.setContent(messageDetails.getContent());
        return messageRepository.save(message);
    }

    // Update - Partial update
    public Message patchMessage(Long id, Message messageDetails) {
        Message message = getMessageById(id);
        
        if (messageDetails.getBooking() != null) {
            message.setBooking(messageDetails.getBooking());
        }
        if (messageDetails.getSender() != null) {
            message.setSender(messageDetails.getSender());
        }
        if (messageDetails.getReceiver() != null) {
            message.setReceiver(messageDetails.getReceiver());
        }
        if (messageDetails.getContent() != null) {
            message.setContent(messageDetails.getContent());
        }
        return messageRepository.save(message);
    }

    // Delete
    public void deleteMessage(Long id) {
        Message message = getMessageById(id);
        messageRepository.delete(message);
    }
}



