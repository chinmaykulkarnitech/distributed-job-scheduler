package com.chinmaytech.job_scheduler_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "worker_heartbeats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "WorkerHeartbeat",
        description = "Records periodic heartbeat information and resource usage metrics reported by a worker."
)
public class WorkerHeartbeat {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the heartbeat record.",
            example = "f3b7c8d1-2a45-4e91-9c72-6a8b5d123456",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String id;

    @Column(name = "worker_id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the worker that sent this heartbeat.",
            example = "b2d0e4d7-059b-4a6c-9093-93d9d123e241"
    )
    private String workerId;

    @Column(name = "heartbeat_at", nullable = false)
    @Schema(
            description = "Timestamp when the worker heartbeat was received or recorded.",
            example = "2026-08-24T15:30:00"
    )
    private LocalDateTime heartbeatAt;

    @Column(name = "cpu_usage", precision = 5, scale = 2)
    @Schema(
            description = "CPU utilization percentage reported by the worker.",
            example = "42.75",
            minimum = "0",
            maximum = "100"
    )
    private BigDecimal cpuUsage;

    @Column(name = "memory_usage", precision = 5, scale = 2)
    @Schema(
            description = "Memory utilization percentage reported by the worker.",
            example = "68.50",
            minimum = "0",
            maximum = "100"
    )
    private BigDecimal memoryUsage;

    @Column(name = "active_jobs", nullable = false)
    @Schema(
            description = "Number of jobs currently being executed by the worker.",
            example = "2",
            minimum = "0"
    )
    private Integer activeJobs;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Timestamp when the heartbeat record was created.",
            example = "2026-08-24T15:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (heartbeatAt == null) {
            heartbeatAt = LocalDateTime.now();
        }

        if (activeJobs == null) {
            activeJobs = 0;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}