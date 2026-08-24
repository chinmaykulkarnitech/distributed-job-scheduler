package com.chinmaytech.job_scheduler_backend.dto.Job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "JobResponse",
        description = "Response representing a job and its current execution state"
)
public class JobResponse {

    @Schema(
            description = "Unique identifier of the job",
            example = "0753588f-3c67-4a73-9882-81ee85bb7ce3"
    )
    private String id;

    @Schema(
            description = "Unique identifier of the queue containing the job",
            example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
    )
    private String queueId;

    @Schema(
            description = "Type of job to be executed",
            example = "EMAIL"
    )
    private String jobType;

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

    @Schema(
            description = "Priority assigned to the job. Higher values indicate higher priority",
            example = "10",
            minimum = "0"
    )
    private Integer priority;

    @Schema(
            description = "JSON payload containing the data required for job execution",
            example = "{\"to\":\"user@example.com\",\"subject\":\"Welcome\"}"
    )
    private String payload;

    @Schema(
            description = "Scheduled execution time of the job",
            example = "2026-08-24T20:00:00"
    )
    private LocalDateTime runAt;

    @Schema(
            description = "Number of execution attempts made for the job",
            example = "1",
            minimum = "0"
    )
    private Integer attemptCount;

    @Schema(
            description = "Date and time when the job was created",
            example = "2026-08-24T02:43:32"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the job was last updated",
            example = "2026-08-24T02:50:15"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Idempotency key used to prevent duplicate job submissions",
            example = "POST-TEST-NEW-001"
    )
    private String idempotencyKey;

    @Schema(
            description = "Unique identifier of the worker that claimed the job. Null when the job has not been claimed",
            example = "84475c92-fa63-4344-8b02-cec33ade9ed4",
            nullable = true
    )
    private String claimedBy;

    @Schema(
            description = "Date and time when the job was claimed by a worker. Null when the job has not been claimed",
            example = "2026-08-24T02:45:10",
            nullable = true
    )
    private LocalDateTime claimedAt;
}