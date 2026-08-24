package com.chinmaytech.job_scheduler_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "jobs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jobs_queue_idempotency",
                        columnNames = {"queue_id", "idempotency_key"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "Job",
        description = "Entity representing an asynchronous background job managed by the scheduler"
)
public class Job {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the job",
            example = "0753588f-3c67-4a73-9882-81ee85bb7ce3"
    )
    private String id;


    @Column(name = "queue_id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the queue to which the job belongs",
            example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
    )
    private String queueId;


    @Column(name = "job_type", length = 100, nullable = false)
    @Schema(
            description = "Type of job to be executed",
            example = "TEST_POST"
    )
    private String jobType;


    @Column(name = "status", length = 30, nullable = false)
    @Schema(
            description = "Current execution status of the job",
            example = "QUEUED",
            allowableValues = {
                    "QUEUED",
                    "RUNNING",
                    "COMPLETED",
                    "FAILED"
            }
    )
    private String status;


    @Column(name = "priority", nullable = false)
    @Schema(
            description = "Priority of the job. Higher values represent higher priority",
            example = "10",
            minimum = "0"
    )
    private Integer priority;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "json"
    )
    @Schema(
            description = "JSON payload containing the data required to execute the job",
            example = "{\"test\":\"post-working\"}"
    )
    private String payload;


    @Column(name = "run_at", nullable = false)
    @Schema(
            description = "Date and time at which the job becomes eligible for execution",
            example = "2026-08-24T20:00:00"
    )
    private LocalDateTime runAt;


    @Column(name = "attempt_count", nullable = false)
    @Schema(
            description = "Number of execution attempts made for this job",
            example = "0",
            minimum = "0"
    )
    private Integer attemptCount;


    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Date and time when the job was created",
            example = "2026-08-24T02:43:32"
    )
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Date and time when the job was last updated",
            example = "2026-08-24T02:45:10"
    )
    private LocalDateTime updatedAt;


    @Column(name = "idempotency_key", length = 255)
    @Schema(
            description = "Optional key used to prevent duplicate job creation",
            example = "POST-TEST-NEW-001",
            nullable = true
    )
    private String idempotencyKey;


    @Column(name = "claimed_by", length = 36)
    @Schema(
            description = "Identifier of the worker currently processing the job",
            example = "worker-001",
            nullable = true
    )
    private String claimedBy;


    @Column(name = "claimed_at")
    @Schema(
            description = "Date and time when the job was claimed by a worker",
            example = "2026-08-24T20:01:15",
            nullable = true
    )
    private LocalDateTime claimedAt;


    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (status == null) {
            status = "QUEUED";
        }

        if (priority == null) {
            priority = 0;
        }

        if (attemptCount == null) {
            attemptCount = 0;
        }

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (runAt == null) {
            runAt = now;
        }
    }


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
