package com.chinmaytech.job_scheduler_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Worker",
        description = "Represents a worker responsible for claiming and executing background jobs."
)
public class Worker {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the worker.",
            example = "b2d0e4d7-059b-4a6c-9093-93d9d123e241",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    @Schema(
            description = "Human-readable name of the worker.",
            example = "Worker-01"
    )
    private String name;

    @Column(name = "status", length = 20, nullable = false)
    @Schema(
            description = "Current status of the worker.",
            example = "ONLINE",
            allowableValues = {"ONLINE", "OFFLINE"}
    )
    private String status;

    @Column(name = "hostname", length = 255)
    @Schema(
            description = "Hostname or machine identifier where the worker is running.",
            example = "worker-node-01"
    )
    private String hostname;

    @Column(name = "last_heartbeat_at")
    @Schema(
            description = "Timestamp of the worker's most recent heartbeat.",
            example = "2026-08-24T15:30:00"
    )
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "started_at", nullable = false)
    @Schema(
            description = "Timestamp when the worker started.",
            example = "2026-08-24T14:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime startedAt;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Timestamp when the worker record was created.",
            example = "2026-08-24T14:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Timestamp when the worker record was last updated.",
            example = "2026-08-24T15:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    @Column(name = "concurrency_limit", nullable = false)
    @Schema(
            description = "Maximum number of jobs that this worker can execute concurrently.",
            example = "3",
            minimum = "1"
    )
    private Integer concurrencyLimit;

    @Column(name = "active_jobs", nullable = false)
    @Schema(
            description = "Number of jobs currently being executed by this worker.",
            example = "2",
            minimum = "0",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer activeJobs;

    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (status == null) {
            status = "ONLINE";
        }

        if (concurrencyLimit == null) {
            concurrencyLimit = 1;
        }

        if (activeJobs == null) {
            activeJobs = 0;
        }

        LocalDateTime now = LocalDateTime.now();

        if (startedAt == null) {
            startedAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (lastHeartbeatAt == null) {
            lastHeartbeatAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}