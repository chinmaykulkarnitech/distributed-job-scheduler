package com.chinmaytech.job_scheduler_backend.controller;

import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
import com.chinmaytech.job_scheduler_backend.entity.Worker;
import com.chinmaytech.job_scheduler_backend.entity.WorkerHeartbeat;
import com.chinmaytech.job_scheduler_backend.service.JobService;
import com.chinmaytech.job_scheduler_backend.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@Tag(
        name = "Workers",
        description = "APIs for worker registration, health monitoring, job claiming, and job execution"
)
@SecurityRequirement(name = "bearerAuth")
public class WorkerController {

    private final WorkerService workerService;
    private final JobService jobService;

    // REGISTER WORKER
    // POST /api/workers

    @Operation(
            summary = "Register a worker",
            description = "Registers a new worker instance with the job scheduler. " +
                    "The returned worker ID is used for heartbeats and job execution."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Worker registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Worker name is invalid"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Worker> registerWorker(
            @Parameter(
                    description = "Name of the worker instance",
                    required = true,
                    example = "worker-A"
            )
            @RequestParam String name
    ) {

        Worker worker = workerService.registerWorker(name);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(worker);
    }

    @Operation(
            summary = "Get worker by ID",
            description = "Retrieves the details and current state of a specific worker."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Worker retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Worker not found"
            )
    })
    // GET ONE WORKER
    // GET /api/workers/{workerId}
    @GetMapping("/{workerId}")
    public ResponseEntity<Worker> getWorker(
            @Parameter(
                    description = "Unique identifier of the worker",
                    required = true,
                    example = "84475c92-fa63-4344-8b02-cec33ade9ed4"
            )
            @PathVariable String workerId
    ) {

        Worker worker = workerService.getWorker(workerId);

        return ResponseEntity.ok(worker);
    }


    // GET ONLINE WORKERS
    // GET /api/workers/online
    @Operation(
            summary = "Get online workers",
            description = "Retrieves all workers that are currently considered online based on their heartbeat status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Online workers retrieved successfully"
            )
    })
    @GetMapping("/online")
    public ResponseEntity<List<Worker>> getOnlineWorkers() {

        List<Worker> workers =
                workerService.getOnlineWorkers();

        return ResponseEntity.ok(workers);
    }

    @Operation(
            summary = "Send worker heartbeat",
            description = "Updates the worker's heartbeat and health information. " +
                    "CPU usage, memory usage, and the number of currently active jobs can be reported."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Worker heartbeat recorded successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Worker not found"
            )
    })
    @PostMapping("/{workerId}/heartbeat")
    public ResponseEntity<WorkerHeartbeat> heartbeat(
            @Parameter(
                    description = "Unique identifier of the worker",
                    required = true,
                    example = "84475c92-fa63-4344-8b02-cec33ade9ed4"
            )

            @PathVariable String workerId,
            @Parameter(
                    description = "Current CPU utilization of the worker",
                    example = "35.50"
            )
            @RequestParam(required = false) BigDecimal cpuUsage,

            @Parameter(
                    description = "Current memory utilization of the worker",
                    example = "62.30"
            )
            @RequestParam(required = false) BigDecimal memoryUsage,
            @Parameter(
                    description = "Number of jobs currently being processed by the worker",
                    example = "2"
            )
            @RequestParam(defaultValue = "0") Integer activeJobs
    ) {

        WorkerHeartbeat heartbeat =
                workerService.sendHeartbeat(
                        workerId,
                        cpuUsage,
                        memoryUsage,
                        activeJobs
                );

        return ResponseEntity.ok(heartbeat);
    }
    @Operation(
            summary = "Claim a job",
            description = "Attempts to atomically claim the next available job for the specified worker. " +
                    "The scheduler ensures that a job is not simultaneously claimed by multiple workers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Job claimed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No available job found or worker not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Job could not be claimed because it was already claimed"
            )
    })
    @PostMapping("/{workerId}/claim")
    public ResponseEntity<JobResponse> claimJob(
            @Parameter(
                    description = "Unique identifier of the worker claiming the job",
                    required = true,
                    example = "84475c92-fa63-4344-8b02-cec33ade9ed4"
            )
            @PathVariable String workerId
    ) {

        JobResponse response =
                jobService.claimJob(workerId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Complete a job",
            description = "Marks a claimed job as COMPLETED after successful execution by the specified worker."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Job completed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job or worker not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Job is not assigned to the specified worker or cannot be completed"
            )
    })
    @PostMapping("/{workerId}/jobs/{jobId}/complete")
    public ResponseEntity<JobResponse> completeJob(
            @Parameter(
                    description = "Unique identifier of the worker executing the job",
                    required = true,
                    example = "84475c92-fa63-4344-8b02-cec33ade9ed4"
            )
            @PathVariable String workerId,

            @Parameter(
                    description = "Unique identifier of the job to complete",
                    required = true,
                    example = "3e3e0b7b-3e6f-4f81-8edb-cc1f3e20e4e4"
            )
            @PathVariable String jobId
    ) {

        JobResponse response =
                jobService.completeJob(jobId, workerId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Fail a job",
            description = "Marks a claimed job as failed after unsuccessful execution by the specified worker. " +
                    "The scheduler can then apply the configured retry policy."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Job failure recorded successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job or worker not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Job is not assigned to the specified worker or cannot be failed"
            )
    })
    @PostMapping("/{workerId}/jobs/{jobId}/fail")
    public ResponseEntity<JobResponse> failJob(
            @Parameter(
                    description = "Unique identifier of the worker executing the job",
                    required = true,
                    example = "84475c92-fa63-4344-8b02-cec33ade9ed4"
            )
            @PathVariable String workerId,

            @Parameter(
                    description = "Unique identifier of the job to mark as failed",
                    required = true,
                    example = "61cc9a3b-11b3-4309-9370-6792393c3a17"
            )
            @PathVariable String jobId
    ) {

        JobResponse response =
                jobService.failJob(jobId, workerId);

        return ResponseEntity.ok(response);
    }



}