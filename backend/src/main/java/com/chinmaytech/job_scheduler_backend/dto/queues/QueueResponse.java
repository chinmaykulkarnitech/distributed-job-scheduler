package com.chinmaytech.job_scheduler_backend.dto.queues;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "QueueResponse",
        description = "Response representing a job queue and its configuration"
)
public class QueueResponse {

    @Schema(
            description = "Unique identifier of the queue",
            example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
    )
    private String id;

    @Schema(
            description = "Unique identifier of the project that owns the queue",
            example = "4f939133-d88f-4d51-96a3-834def8a32ee"
    )
    private String projectId;

    @Schema(
            description = "Unique identifier of the retry policy assigned to the queue",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73",
            nullable = true
    )
    private String retryPolicyId;

    @Schema(
            description = "Name of the queue",
            example = "email-processing"
    )
    private String name;

    @Schema(
            description = "Default priority associated with the queue",
            example = "10",
            minimum = "0"
    )
    private Integer priority;

    @Schema(
            description = "Maximum number of jobs that can be processed concurrently from this queue",
            example = "5",
            minimum = "1"
    )
    private Integer concurrencyLimit;

    @Schema(
            description = "Current status of the queue",
            example = "ACTIVE"
    )
    private String status;

    @Schema(
            description = "Date and time when the queue was created",
            example = "2026-08-24T03:20:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the queue was last updated",
            example = "2026-08-24T03:25:00"
    )
    private LocalDateTime updatedAt;
}