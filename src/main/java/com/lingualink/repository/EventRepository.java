package com.lingualink.repository;

import com.lingualink.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer_Id(Long organizerId);
    List<Event> findByLanguage(String language);
    
    // Pagination support
    Page<Event> findAll(Pageable pageable);
    Page<Event> findByOrganizer_Id(Long organizerId, Pageable pageable);
    Page<Event> findByLanguage(String language, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT e FROM Event e WHERE " +
           "(:organizerId IS NULL OR e.organizer.id = :organizerId) AND " +
           "(:language IS NULL OR LOWER(e.language) LIKE LOWER(CONCAT('%', :language, '%'))) AND " +
           "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDateFrom IS NULL OR e.startDatetime >= :startDateFrom) AND " +
           "(:startDateTo IS NULL OR e.startDatetime <= :startDateTo)")
    Page<Event> findByFilters(@Param("organizerId") Long organizerId,
                              @Param("language") String language,
                              @Param("title") String title,
                              @Param("location") String location,
                              @Param("startDateFrom") LocalDateTime startDateFrom,
                              @Param("startDateTo") LocalDateTime startDateTo,
                              Pageable pageable);
}

