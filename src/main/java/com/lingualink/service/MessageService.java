package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.dto.request.MessageRequest;
import com.lingualink.dto.response.MessageResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Message;
import com.lingualink.entity.User;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.mapper.MessageMapper;
import com.lingualink.repository.BookingRepository;
import com.lingualink.repository.MessageRepository;
import com.lingualink.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, MessageMapper messageMapper,
                         BookingRepository bookingRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Create
    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    public MessageResponse createMessage(MessageRequest request) {
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        User sender = request.getSenderId() != null
                ? userRepository.findById(request.getSenderId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getSenderId()))
                : null;
        User receiver = request.getReceiverId() != null
                ? userRepository.findById(request.getReceiverId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReceiverId()))
                : null;
        Message message = messageMapper.toEntity(request, booking, sender, receiver);
        Message savedMessage = messageRepository.save(message);
        return messageMapper.toResponse(savedMessage);
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
    public PagedResponse<MessageResponse> getAllMessagesWithFiltersAsResponse(PageParams pageParams, Long bookingId, Long senderId, 
                                                                               Long receiverId, String content,
                                                                               LocalDateTime sentFrom, LocalDateTime sentTo) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findByFilters(bookingId, senderId, receiverId, content, 
                                                              sentFrom, sentTo, pageable);
        List<MessageResponse> contentList = page.getContent().stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(contentList, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessagesAsResponse() {
        List<Message> messages = getAllMessages();
        return messages.stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageByIdAsResponse(Long id) {
        Message message = getMessageById(id);
        return messageMapper.toResponse(message);
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
    public PagedResponse<MessageResponse> getMessagesByBookingIdAsResponse(Long bookingId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findByBooking_Id(bookingId, pageable);
        List<MessageResponse> content = page.getContent().stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByBookingIdAsResponse(Long bookingId) {
        List<Message> messages = getMessagesByBookingId(bookingId);
        return messages.stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getMessagesBySenderAndReceiverAsResponse(Long senderId, Long receiverId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("sentAt");
        Page<Message> page = messageRepository.findBySender_IdAndReceiver_Id(senderId, receiverId, pageable);
        List<MessageResponse> content = page.getContent().stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesBySenderAndReceiverAsResponse(Long senderId, Long receiverId) {
        List<Message> messages = getMessagesBySenderAndReceiver(senderId, receiverId);
        return messages.stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
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

    public MessageResponse updateMessage(Long id, MessageRequest request) {
        Message message = getMessageById(id);
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : message.getBooking();
        User sender = request.getSenderId() != null
                ? userRepository.findById(request.getSenderId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getSenderId()))
                : message.getSender();
        User receiver = request.getReceiverId() != null
                ? userRepository.findById(request.getReceiverId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReceiverId()))
                : message.getReceiver();
        messageMapper.updateEntityFromRequest(request, message, booking, sender, receiver);
        Message savedMessage = messageRepository.save(message);
        return messageMapper.toResponse(savedMessage);
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

    public MessageResponse patchMessage(Long id, MessageRequest request) {
        Message message = getMessageById(id);
        Booking booking = request.getBookingId() != null
                ? bookingRepository.findById(request.getBookingId())
                        .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()))
                : null;
        User sender = request.getSenderId() != null
                ? userRepository.findById(request.getSenderId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getSenderId()))
                : null;
        User receiver = request.getReceiverId() != null
                ? userRepository.findById(request.getReceiverId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReceiverId()))
                : null;
        messageMapper.updateEntityFromRequest(request, message, booking, sender, receiver);
        Message savedMessage = messageRepository.save(message);
        return messageMapper.toResponse(savedMessage);
    }

    // Delete
    public void deleteMessage(Long id) {
        Message message = getMessageById(id);
        messageRepository.delete(message);
    }
}



