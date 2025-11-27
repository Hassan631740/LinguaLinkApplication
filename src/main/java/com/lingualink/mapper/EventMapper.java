package com.lingualink.mapper;

import com.lingualink.dto.request.EventRequest;
import com.lingualink.dto.response.EventResponse;
import com.lingualink.entity.Event;
import com.lingualink.entity.User;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequest request, User organizer) {
        if (request == null) {
            return null;
        }
        Event event = new Event();
        event.setOrganizer(organizer);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDatetime(request.getStartDatetime());
        event.setEndDatetime(request.getEndDatetime());
        event.setLocation(request.getLocation());
        event.setLanguage(request.getLanguage());
        return event;
    }

    public EventResponse toResponse(Event event) {
        if (event == null) {
            return null;
        }
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setOrganizerId(event.getOrganizer() != null ? event.getOrganizer().getId() : null);
        response.setOrganizerName(event.getOrganizer() != null ? event.getOrganizer().getName() : null);
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setStartDatetime(event.getStartDatetime());
        response.setEndDatetime(event.getEndDatetime());
        response.setLocation(event.getLocation());
        response.setLanguage(event.getLanguage());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }

    public void updateEntityFromRequest(EventRequest request, Event event, User organizer) {
        if (request == null || event == null) {
            return;
        }
        if (organizer != null) {
            event.setOrganizer(organizer);
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartDatetime() != null) {
            event.setStartDatetime(request.getStartDatetime());
        }
        if (request.getEndDatetime() != null) {
            event.setEndDatetime(request.getEndDatetime());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getLanguage() != null) {
            event.setLanguage(request.getLanguage());
        }
    }
}

