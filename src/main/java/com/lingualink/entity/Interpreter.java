package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "interpreters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interpreter extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "json")
    private String languages;

    @Column(name = "rate_per_hour")
    private BigDecimal ratePerHour;
    
    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * JSON array of certificate file URLs
     * Example: ["/uploads/certificates/cert1.pdf", "/uploads/certificates/cert2.pdf"]
     */
    @Column(name = "certificate_urls", columnDefinition = "json")
    private String certificateUrls;
}
