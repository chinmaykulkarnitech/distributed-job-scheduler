package com.chinmaytech.job_scheduler_backend.dto.Job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "BatchJobResponse",
        description = "Response returned after submitting multiple jobs as a batch"
)
public class BatchJobResponse {

    @Schema(
            description = "Total number of jobs submitted in the batch",
            example = "5"
    )
    private int totalJobs;

    @Schema(
            description = "Number of jobs successfully created",
            example = "5"
    )
    private int successfulJobs;

    @Schema(
            description = "Number of jobs that failed to be created",
            example = "0"
    )
    private int failedJobs;

    @Schema(
            description = "List of successfully created jobs"
    )
    private List<JobResponse> jobs;

    @Schema(
            description = "List of errors encountered while creating jobs"
    )
    private List<String> errors;
}