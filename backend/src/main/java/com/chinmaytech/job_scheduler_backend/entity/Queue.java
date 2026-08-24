package com.chinmaytech.job_scheduler_backend.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Queue",
        description = "Represents a job queue belonging to a project. Queues control job priority, concurrency, and retry policy."
)
public class Queue {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the queue",
            example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
    )
    private String id;


    @Column(name = "project_id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the project that owns this queue",
            example = "a52eec47-18f8-4016-af20-2c06a74c3498"
    )
    private String projectId;


    @Column(name = "retry_policy_id", length = 36)
    @Schema(
            description = "Unique identifier of the retry policy assigned to this queue",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73",
            nullable = true
    )
    private String retryPolicyId;


    @Column(name = "name", length = 100, nullable = false)
    @Schema(
            description = "Name of the queue",
            example = "email-processing"
    )
    private String name;


    @Column(name = "priority", nullable = false)
    @Schema(
            description = "Default priority assigned to jobs in the queue. Higher values can be used for higher-priority jobs.",
            example = "10",
            minimum = "0"
    )
    private Integer priority;


    @Column(name = "concurrency_limit", nullable = false)
    @Schema(
            description = "Maximum number of jobs that can be processed concurrently from this queue",
            example = "5",
            minimum = "1"
    )
    private Integer concurrencyLimit;


    @Column(name = "status", length = 20, nullable = false)
    @Schema(
            description = "Current status of the queue",
            example = "ACTIVE",
            allowableValues = {
                    "ACTIVE",
                    "PAUSED"
            }
    )
    private String status;


    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Date and time when the queue was created",
            example = "2026-08-24T02:40:00"
    )
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Date and time when the queue was last updated",
            example = "2026-08-24T02:45:00"
    )
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (priority == null) {
            priority = 0;
        }

        if (concurrencyLimit == null) {
            concurrencyLimit = 1;
        }

        if (status == null) {
            status = "ACTIVE";
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
