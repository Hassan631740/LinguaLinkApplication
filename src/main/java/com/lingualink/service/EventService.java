package com.lingualink.service;

import com.lingualink.dto.PageParams;
import com.lingualink.dto.PagedResponse;
import com.lingualink.entity.Event;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.EventRepository;
import com.lingualink.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Create
    public Event createEvent(Event event) {
        validateEventDates(event);
        return eventRepository.save(event);
    }

    // Read
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagedResponse<Event> getAllEvents(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("id");
        Page<Event> page = eventRepository.findAll(pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Event> getAllEventsWithFilters(PageParams pageParams, Long organizerId, String language, 
                                                         String title, String location, 
                                                         LocalDateTime startDateFrom, LocalDateTime startDateTo) {
        Pageable pageable = pageParams.toPageable("startDatetime");
        Page<Event> page = eventRepository.findByFilters(organizerId, language, title, location, 
                                                          startDateFrom, startDateTo, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByOrganizerId(Long organizerId) {
        return eventRepository.findByOrganizer_Id(organizerId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Event> getEventsByOrganizerId(Long organizerId, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("startDatetime");
        Page<Event> page = eventRepository.findByOrganizer_Id(organizerId, pageable);
        return PagedResponse.of(page);
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByLanguage(String language) {
        return eventRepository.findByLanguage(language);
    }

    @Transactional(readOnly = true)
    public PagedResponse<Event> getEventsByLanguage(String language, PageParams pageParams) {
        Pageable pageable = pageParams.toPageable("startDatetime");
        Page<Event> page = eventRepository.findByLanguage(language, pageable);
        return PagedResponse.of(page);
    }

    // Update - Full update
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);
        validateEventOwnership(event);
        event.setOrganizer(eventDetails.getOrganizer());
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDatetime(eventDetails.getStartDatetime());
        event.setEndDatetime(eventDetails.getEndDatetime());
        event.setLocation(eventDetails.getLocation());
        event.setLanguage(eventDetails.getLanguage());
        validateEventDates(event);
        return eventRepository.save(event);
    }

    // Update - Partial update
    public Event patchEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);
        validateEventOwnership(event);
        
        if (eventDetails.getOrganizer() != null) {
            event.setOrganizer(eventDetails.getOrganizer());
        }
        if (eventDetails.getTitle() != null) {
            event.setTitle(eventDetails.getTitle());
        }
        if (eventDetails.getDescription() != null) {
            event.setDescription(eventDetails.getDescription());
        }
        if (eventDetails.getStartDatetime() != null) {
            event.setStartDatetime(eventDetails.getStartDatetime());
        }
        if (eventDetails.getEndDatetime() != null) {
            event.setEndDatetime(eventDetails.getEndDatetime());
        }
        if (eventDetails.getLocation() != null) {
            event.setLocation(eventDetails.getLocation());
        }
        if (eventDetails.getLanguage() != null) {
            event.setLanguage(eventDetails.getLanguage());
        }
        validateEventDates(event);
        return eventRepository.save(event);
    }

    // Delete
    public void deleteEvent(Long id) {
        Event event = getEventById(id);
        validateEventOwnership(event);
        eventRepository.delete(event);
    }

    private void validateEventDates(Event event) {
        if (event.getStartDatetime() != null && event.getEndDatetime() != null) {
            if (event.getEndDatetime().isBefore(event.getStartDatetime())) {
                throw new IllegalArgumentException("End datetime must be after start datetime");
            }
        }
    }

    private void validateEventOwnership(Event event) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        // Administrators can access any event
        if (SecurityUtils.isAdministrator()) {
            return;
        }
        
        // Check if current user is the organizer
        if (event.getOrganizer() == null || !event.getOrganizer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to modify this event");
        }
    }
}

