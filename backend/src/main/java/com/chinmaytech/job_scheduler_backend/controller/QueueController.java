package com.chinmaytech.job_scheduler_backend.controller;


import com.chinmaytech.job_scheduler_backend.dto.queues.CreateQueueRequest;
import com.chinmaytech.job_scheduler_backend.dto.queues.QueueResponse;
import com.chinmaytech.job_scheduler_backend.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@Tag(
        name = "Queues",
        description = "APIs for creating, retrieving, and configuring job queues"
)
public class QueueController {

    private final QueueService queueService;
    @Operation(
            summary = "Create a new queue",
            description = "Creates a new job queue for a project using the provided queue configuration."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Queue created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid queue request"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Queue already exists"
            )
    })
    @PostMapping
    public ResponseEntity<QueueResponse> createQueue(
            @Valid @RequestBody CreateQueueRequest request
    ) {

        QueueResponse response =
                queueService.createQueue(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @Operation(
            summary = "Get queues by project",
            description = "Retrieves all job queues associated with the specified project."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Queues retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<QueueResponse>> getQueuesByProject(
            @Parameter(
                    description = "Unique identifier of the project",
                    required = true,
                    example = "a52eec47-18f8-4016-af20-2c06a74c3498"
            )
            @PathVariable String projectId
    ) {

        List<QueueResponse> queues =
                queueService.getQueuesByProject(projectId);

        return ResponseEntity.ok(queues);
    }

    @Operation(
            summary = "Get queue by ID",
            description = "Retrieves the details and configuration of a specific job queue."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Queue retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Queue not found"
            )
    })

    @GetMapping("/{queueId}")
    public ResponseEntity<QueueResponse> getQueue(
            @Parameter(
                    description = "Unique identifier of the queue",
                    required = true,
                    example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
            )
            @PathVariable String queueId
    ) {

        QueueResponse response =
                queueService.getQueue(queueId);

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Assign retry policy to queue",
            description = "Associates an existing retry policy with the specified job queue. The retry policy controls how failed jobs are retried."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retry policy assigned successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Queue or retry policy not found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid queue or retry policy"
            )
    })
    @PutMapping("/{queueId}/retry-policy/{retryPolicyId}")
    public ResponseEntity<QueueResponse> assignRetryPolicy(
            @Parameter(
                    description = "Unique identifier of the queue",
                    required = true,
                    example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
            )
            @PathVariable String queueId,
            @Parameter(
                    description = "Unique identifier of the retry policy",
                    required = true,
                    example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
            )
            @PathVariable String retryPolicyId
    ) {

        QueueResponse response =
                queueService.assignRetryPolicy(
                        queueId,
                        retryPolicyId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Pause a queue",
            description = "Updates the status of the specified queue to PAUSED. Jobs in a paused queue cannot be claimed by workers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Queue paused successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Queue not found"
            )
    })
    @PutMapping("/{queueId}/pause")
    public ResponseEntity<QueueResponse> pauseQueue(
            @Parameter(
                    description = "Unique identifier of the queue",
                    required = true,
                    example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
            )
            @PathVariable String queueId
    ) {
        QueueResponse response = queueService.pauseQueue(queueId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Resume a queue",
            description = "Updates the status of the specified queue to ACTIVE. Jobs in the queue become eligible for claiming by workers."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Queue resumed successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Queue not found"
            )
    })
    @PutMapping("/{queueId}/resume")
    public ResponseEntity<QueueResponse> resumeQueue(
            @Parameter(
                    description = "Unique identifier of the queue",
                    required = true,
                    example = "8cc8e6ec-0edf-468f-b265-cc19f71d3b45"
            )
            @PathVariable String queueId
    ) {
        QueueResponse response = queueService.resumeQueue(queueId);
        return ResponseEntity.ok(response);
    }


}

