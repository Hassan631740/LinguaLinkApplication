package com.lingualink.service;

import com.lingualink.entity.Event;
import com.lingualink.exception.ResourceNotFoundException;
import com.lingualink.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByOrganizerId(Long organizerId) {
        return eventRepository.findByOrganizer_Id(organizerId);
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByLanguage(String language) {
        return eventRepository.findByLanguage(language);
    }

    // Update - Full update
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);
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
        eventRepository.delete(event);
    }

    private void validateEventDates(Event event) {
        if (event.getStartDatetime() != null && event.getEndDatetime() != null) {
            if (event.getEndDatetime().isBefore(event.getStartDatetime())) {
                throw new IllegalArgumentException("End datetime must be after start datetime");
            }
        }
    }
}

