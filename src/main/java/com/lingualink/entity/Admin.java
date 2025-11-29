package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "department")
    private String department;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "access_level")
    private String accessLevel; // SUPER_ADMIN, ADMIN, MODERATOR

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String permissions; // JSON string of permissions

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

