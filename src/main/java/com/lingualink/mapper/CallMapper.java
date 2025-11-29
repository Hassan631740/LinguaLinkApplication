package com.lingualink.mapper;

import com.lingualink.dto.request.CallRequest;
import com.lingualink.dto.response.CallResponse;
import com.lingualink.entity.Booking;
import com.lingualink.entity.Call;
import com.lingualink.entity.Interpreter;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CallMapper {

    public Call toEntity(CallRequest request, Booking booking, User client, Interpreter interpreter) {
        if (request == null) {
            return null;
        }
        Call call = new Call();
        call.setBooking(booking);
        call.setClient(client);
        call.setInterpreter(interpreter);
        call.setCallDurationSeconds(request.getCallDurationSeconds());
        call.setStartTime(request.getStartTime());
        call.setEndTime(request.getEndTime());
        call.setStatus(request.getStatus());
        call.setRecordingUrl(request.getRecordingUrl());
        call.setNotes(request.getNotes());
        call.setQualityRating(request.getQualityRating());
        return call;
    }

    public CallResponse toResponse(Call call) {
        if (call == null) {
            return null;
        }
        CallResponse response = new CallResponse();
        response.setId(call.getId());
        response.setBookingId(call.getBooking() != null ? call.getBooking().getId() : null);
        response.setBookingTitle(call.getBooking() != null && call.getBooking().getEvent() != null 
                ? call.getBooking().getEvent().getTitle() : null);
        response.setClientId(call.getClient() != null ? call.getClient().getId() : null);
        response.setClientName(call.getClient() != null ? call.getClient().getName() : null);
        response.setInterpreterId(call.getInterpreter() != null ? call.getInterpreter().getId() : null);
        response.setInterpreterName(call.getInterpreter() != null && call.getInterpreter().getUser() != null 
                ? call.getInterpreter().getUser().getName() : null);
        response.setCallDurationSeconds(call.getCallDurationSeconds());
        response.setStartTime(call.getStartTime());
        response.setEndTime(call.getEndTime());
        response.setStatus(call.getStatus());
        response.setRecordingUrl(call.getRecordingUrl());
        response.setNotes(call.getNotes());
        response.setQualityRating(call.getQualityRating());
        response.setCreatedAt(call.getCreatedAt());
        response.setUpdatedAt(call.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(CallRequest request, Call call, Booking booking, User client, Interpreter interpreter) {
        if (request == null || call == null) {
            return;
        }
        if (booking != null) {
            call.setBooking(booking);
        }
        if (client != null) {
            call.setClient(client);
        }
        if (interpreter != null) {
            call.setInterpreter(interpreter);
        }
        if (request.getCallDurationSeconds() != null) {
            call.setCallDurationSeconds(request.getCallDurationSeconds());
        }
        if (request.getStartTime() != null) {
            call.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            call.setEndTime(request.getEndTime());
        }
        if (request.getStatus() != null) {
            call.setStatus(request.getStatus());
        }
        if (request.getRecordingUrl() != null) {
            call.setRecordingUrl(request.getRecordingUrl());
        }
        if (request.getNotes() != null) {
            call.setNotes(request.getNotes());
        }
        if (request.getQualityRating() != null) {
            call.setQualityRating(request.getQualityRating());
        }
    }
}

