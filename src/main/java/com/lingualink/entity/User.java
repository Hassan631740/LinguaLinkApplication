package com.lingualink.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}
