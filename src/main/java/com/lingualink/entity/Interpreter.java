package com.lingualink.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "interpreters")
public class Interpreter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "json")
    private String languages;

    private BigDecimal ratePerHour;
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // getters/setters omitted for brevity
}
