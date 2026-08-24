package com.chinmaytech.job_scheduler_backend.dto.Job;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CreateJobRequest",
        description = "Request payload used to create a new asynchronous job"
)
public class CreateJobRequest {

    @NotBlank(message = "Queue ID is required")
    @Schema(
            description = "Unique identifier of the queue where the job will be submitted",
            example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String queueId;


    @NotBlank(message = "Job type is required")
    @Size(
            max = 100,
            message = "Job type must not exceed 100 characters"
    )
    @Schema(
            description = "Type of job that the worker should execute",
            example = "EMAIL",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String jobType;


    @Min(
            value = 0,
            message = "Priority cannot be negative"
    )
    @Schema(
            description = "Priority of the job. Higher values indicate higher priority",
            example = "10",
            minimum = "0",
            defaultValue = "5"
    )
    private Integer priority;


    @NotBlank(message = "Payload is required")
    @Schema(
            description = "JSON payload containing the data required to execute the job",
            example = "{\"to\":\"user@example.com\",\"subject\":\"Welcome\"}",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String payload;


    @Schema(
            description = "Date and time when the job becomes eligible for execution. If omitted, the job can run immediately",
            example = "2026-08-24T20:00:00"
    )
    private LocalDateTime runAt;


    @Size(
            max = 255,
            message = "Idempotency key must not exceed 255 characters"
    )
    @Schema(
            description = "Optional unique key used to prevent accidental duplicate job submissions",
            example = "EMAIL-WELCOME-001",
            maxLength = 255
    )
    private String idempotencyKey;
}