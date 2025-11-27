package com.lingualink.mapper;

import com.lingualink.dto.request.BookingRequest;
import com.lingualink.dto.response.BookingResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Event;
import com.lingualink.entity.Interpreter;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public Booking toEntity(BookingRequest request, Event event, Interpreter interpreter) {
        if (request == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setInterpreter(interpreter);
        booking.setStatus(request.getStatus());
        booking.setPrice(request.getPrice());
        booking.setPaymentId(request.getPaymentId());
        return booking;
    }

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setEventId(booking.getEvent() != null ? booking.getEvent().getId() : null);
        response.setEventTitle(booking.getEvent() != null ? booking.getEvent().getTitle() : null);
        response.setInterpreterId(booking.getInterpreter() != null ? booking.getInterpreter().getId() : null);
        response.setInterpreterName(booking.getInterpreter() != null && booking.getInterpreter().getUser() != null 
                ? booking.getInterpreter().getUser().getName() : null);
        response.setStatus(booking.getStatus());
        response.setRequestedAt(booking.getRequestedAt());
        response.setConfirmedAt(booking.getConfirmedAt());
        response.setPrice(booking.getPrice());
        response.setPaymentId(booking.getPaymentId());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(BookingRequest request, Booking booking, Event event, Interpreter interpreter) {
        if (request == null || booking == null) {
            return;
        }
        if (event != null) {
            booking.setEvent(event);
        }
        if (interpreter != null) {
            booking.setInterpreter(interpreter);
        }
        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }
        if (request.getPrice() != null) {
            booking.setPrice(request.getPrice());
        }
        if (request.getPaymentId() != null) {
            booking.setPaymentId(request.getPaymentId());
        }
    }
}

