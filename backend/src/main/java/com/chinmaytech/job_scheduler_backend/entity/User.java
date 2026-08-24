package com.chinmaytech.job_scheduler_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "User",
        description = "Represents a user account in the job scheduling system."
)
public class User {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the user.",
            example = "b2d0e4d7-059b-4a6c-9093-93d9d123e241",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    @Schema(
            description = "Full name of the user.",
            example = "CHINMAY KULKARNI"
    )
    private String name;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    @Schema(
            description = "Unique email address of the user.",
            example = "user@example.com"
    )
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    @Schema(
            description = "Hashed password of the user. The plain-text password must never be stored.",
            example = "$2a$10$exampleHash",
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Timestamp when the user account was created.",
            example = "2026-08-24T15:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Timestamp when the user account was last updated.",
            example = "2026-08-24T15:10:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}