package com.chinmaytech.job_scheduler_backend.dto.queues;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CreateQueueRequest",
        description = "Request payload used to create a queue within a project"
)
public class CreateQueueRequest {

    @NotBlank(message = "Project ID is required")
    @Schema(
            description = "Unique identifier of the project to which the queue belongs",
            example = "4f939133-d88f-4d51-96a3-834def8a32ee",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String projectId;


    @NotBlank(message = "Queue name is required")
    @Size(
            max = 100,
            message = "Queue name must not exceed 100 characters"
    )
    @Schema(
            description = "Name of the queue",
            example = "email-processing",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;


    @Min(
            value = 0,
            message = "Priority cannot be negative"
    )
    @Schema(
            description = "Default priority assigned to jobs in this queue. Higher values indicate higher priority",
            example = "10",
            minimum = "0"
    )
    private Integer priority;


    @Min(
            value = 1,
            message = "Concurrency limit must be at least 1"
    )
    @Schema(
            description = "Maximum number of jobs that can be processed concurrently from this queue",
            example = "5",
            minimum = "1"
    )
    private Integer concurrencyLimit;


    @Schema(
            description = "Optional unique identifier of the retry policy assigned to this queue",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73",
            nullable = true
    )
    private String retryPolicyId;
}