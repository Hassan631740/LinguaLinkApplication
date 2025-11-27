package com.lingualink.mapper;

import com.lingualink.dto.request.MessageRequest;
import com.lingualink.dto.response.MessageResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Message;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public Message toEntity(MessageRequest request, Booking booking, User sender, User receiver) {
        if (request == null) {
            return null;
        }
        Message message = new Message();
        message.setBooking(booking);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        return message;
    }

    public MessageResponse toResponse(Message message) {
        if (message == null) {
            return null;
        }
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setBookingId(message.getBooking() != null ? message.getBooking().getId() : null);
        response.setSenderId(message.getSender() != null ? message.getSender().getId() : null);
        response.setSenderName(message.getSender() != null ? message.getSender().getName() : null);
        response.setReceiverId(message.getReceiver() != null ? message.getReceiver().getId() : null);
        response.setReceiverName(message.getReceiver() != null ? message.getReceiver().getName() : null);
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(MessageRequest request, Message message, Booking booking, User sender, User receiver) {
        if (request == null || message == null) {
            return;
        }
        if (booking != null) {
            message.setBooking(booking);
        }
        if (sender != null) {
            message.setSender(sender);
        }
        if (receiver != null) {
            message.setReceiver(receiver);
        }
        if (request.getContent() != null) {
            message.setContent(request.getContent());
        }
    }
}

