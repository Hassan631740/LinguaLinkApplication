package com.lingualink.service;

import com.lingualink.entity.Event;
import com.lingualink.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Create
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    // Read
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getEventsByOrganizerId(Long organizerId) {
        return eventRepository.findByOrganizer_Id(organizerId);
    }

    public List<Event> getEventsByLanguage(String language) {
        return eventRepository.findByLanguage(language);
    }

    // Update
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        event.setOrganizer(eventDetails.getOrganizer());
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDatetime(eventDetails.getStartDatetime());
        event.setEndDatetime(eventDetails.getEndDatetime());
        event.setLocation(eventDetails.getLocation());
        event.setLanguage(eventDetails.getLanguage());
        return eventRepository.save(event);
    }

    // Delete
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}

