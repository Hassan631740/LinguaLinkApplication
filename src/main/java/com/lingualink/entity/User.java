package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Role role;

    /**
     * Wallet balance for clients (can be topped up and deducted)
     * Earnings balance for interpreters (accumulates from completed bookings)
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * URL to user's avatar image
     */
    @Column(name = "avatar_url")
    private String avatarUrl;
}
