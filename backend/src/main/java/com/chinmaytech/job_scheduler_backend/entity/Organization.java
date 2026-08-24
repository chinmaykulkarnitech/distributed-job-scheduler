package com.chinmaytech.job_scheduler_backend.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="organizations")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Organization",
        description = "Represents an organization that owns projects in the job scheduling platform"
)
public class Organization {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the organization",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
    )
    private String id;


    @Column(name = "name", length = 150, nullable = false)
    @Schema(
            description = "Name of the organization",
            example = "Chinmay Technologies"
    )
    private String name;


    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Date and time when the organization was created",
            example = "2026-08-24T02:30:00"
    )
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Date and time when the organization was last updated",
            example = "2026-08-24T02:35:00"
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
