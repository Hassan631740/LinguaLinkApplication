package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_datetime")
    private LocalDateTime startDatetime;
    
    @Column(name = "end_datetime")
    private LocalDateTime endDatetime;
    private String location;
    private String language;
}

