package com.chinmaytech.job_scheduler_backend.dto.Job;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CreateBatchJobRequest",
        description = "Request payload used to create multiple asynchronous jobs in a single batch"
)
public class CreateBatchJobRequest {

    @NotEmpty(message = "At least one job is required")
    @Size(
            max = 100,
            message = "A batch cannot contain more than 100 jobs"
    )
    @Valid
    @Schema(
            description = "List of jobs to create",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<CreateJobRequest> jobs;
}