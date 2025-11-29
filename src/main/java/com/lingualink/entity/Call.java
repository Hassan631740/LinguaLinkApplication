package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "calls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Call extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "interpreter_id", nullable = false)
    private Interpreter interpreter;

    @Column(name = "call_duration_seconds")
    private Long callDurationSeconds;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 50)
    private String status; // ACTIVE, COMPLETED, CANCELLED, FAILED

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "quality_rating")
    private Integer qualityRating; // 1-5
}

