package com.chinmaytech.job_scheduler_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Project",
        description = "Represents a project belonging to an organization and containing job queues"
)
public class Project {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the project",
            example = "a52eec47-18f8-4016-af20-2c06a74c3498"
    )
    private String id;


    @Column(name = "organization_id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the organization that owns the project",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
    )
    private String organizationId;


    @Column(name = "name", length = 150, nullable = false)
    @Schema(
            description = "Name of the project",
            example = "Distributed Job Scheduler"
    )
    private String name;


    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(
            description = "Optional description of the project",
            example = "Production-inspired distributed background job scheduling platform"
    )
    private String description;


    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Date and time when the project was created",
            example = "2026-08-22T18:24:36"
    )
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Date and time when the project was last updated",
            example = "2026-08-22T18:30:40"
    )
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}