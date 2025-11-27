package com.lingualink.repository;

import com.lingualink.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer_Id(Long organizerId);
    List<Event> findByLanguage(String language);
}

