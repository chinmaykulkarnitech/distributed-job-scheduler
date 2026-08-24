package com.chinmaytech.job_scheduler_backend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "retry_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "RetryPolicy",
        description = "Defines the retry behavior for failed jobs, including retry strategy, maximum attempts, and delay configuration."
)
public class RetryPolicy {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the retry policy.",
            example = "a8c1e5b7-7f6a-4d42-91f0-3b2f5c9d1234",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    @Schema(
            description = "Name of the retry policy.",
            example = "Default Exponential Retry"
    )
    private String name;

    @Column(name = "strategy", length = 30, nullable = false)
    @Schema(
            description = "Retry strategy used when a job fails. Supported strategies may include FIXED and EXPONENTIAL.",
            example = "EXPONENTIAL",
            allowableValues = {"FIXED", "EXPONENTIAL"}
    )
    private String strategy;

    @Column(name = "max_attempts", nullable = false)
    @Schema(
            description = "Maximum number of attempts allowed for a job, including the initial execution attempt.",
            example = "5",
            minimum = "1"
    )
    private Integer maxAttempts;

    @Column(name = "initial_delay_seconds", nullable = false)
    @Schema(
            description = "Initial delay in seconds before the first retry.",
            example = "10",
            minimum = "0"
    )
    private Integer initialDelaySeconds;

    @Column(name = "max_delay_seconds", nullable = false)
    @Schema(
            description = "Maximum delay in seconds between retry attempts.",
            example = "300",
            minimum = "0"
    )
    private Integer maxDelaySeconds;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Timestamp when the retry policy was created.",
            example = "2026-08-24T15:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(
            description = "Timestamp when the retry policy was last updated.",
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