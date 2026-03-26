package com.smartincident.processor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * User entity for IAM — stored in processor schema.
 */
@Entity
@Table(name = "users", schema = "processor", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // bcrypt encoded

    /**
     * ADMIN — full control
     * DEVELOPER — own incidents only
     */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
