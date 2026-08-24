package com.chinmaytech.job_scheduler_backend.controller;

import com.chinmaytech.job_scheduler_backend.dto.Job.BatchJobResponse;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateBatchJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
import com.chinmaytech.job_scheduler_backend.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(
        name = "Jobs",
        description = "APIs for creating, retrieving, and managing scheduled jobs"
)
public class JobController {

    private final JobService jobService;

    // CREATE JOB
    // POST /api/jobs
    @Operation(
            summary = "Create a new job",
            description = "Creates a new job and places it into the specified queue for asynchronous execution."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Job created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid job request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Queue not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Job with the same idempotency key already exists"
            )
    })
    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestBody CreateJobRequest request,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        JobResponse response =
                jobService.createJob(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ONE JOB
    // GET /api/jobs/{jobId}
    @Operation(
            summary = "Get a job by ID",
            description = "Retrieves the details and current execution status of a specific job."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Job retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job not found"
            )
    })
    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(
            @Parameter(
                    description = "Unique identifier of the job",
                    required = true,
                    example = "0753588f-3c67-4a73-9882-81ee85bb7ce3"
            )
            @PathVariable String jobId,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        JobResponse response =
                jobService.getJob(jobId, userId);

        return ResponseEntity.ok(response);
    }

    // GET ALL JOBS
// GET /api/jobs
    @Operation(
            summary = "Get all jobs",
            description = "Retrieves all jobs accessible to the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs(
            Authentication authentication
    ) {

        String userId = authentication.getName();

        List<JobResponse> responses =
                jobService.getAllJobs(userId);

        return ResponseEntity.ok(responses);
    }


    // GET ALL JOBS OF A QUEUE
    // GET /api/jobs/queue/{queueId}

    @Operation(
            summary = "Get jobs by queue",
            description = "Retrieves all jobs belonging to a specific queue accessible to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Jobs retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Queue not found"
            )
    })
    @GetMapping("/queue/{queueId}")
    public ResponseEntity<List<JobResponse>> getJobsByQueue(
            @Parameter(
                    description = "Unique identifier of the queue",
                    required = true,
                    example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
            )
            @PathVariable String queueId,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        List<JobResponse> responses =
                jobService.getJobsByQueue(queueId, userId);

        return ResponseEntity.ok(responses);
    }


}