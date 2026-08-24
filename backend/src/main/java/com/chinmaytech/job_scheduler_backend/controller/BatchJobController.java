package com.chinmaytech.job_scheduler_backend.controller;

import com.chinmaytech.job_scheduler_backend.dto.Job.BatchJobResponse;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateBatchJobRequest;
import com.chinmaytech.job_scheduler_backend.service.BatchJobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs/batch")
@RequiredArgsConstructor
@Tag(
        name = "Batch Jobs",
        description = "APIs for submitting multiple jobs in a single batch"
)
@SecurityRequirement(name = "bearerAuth")
public class BatchJobController {

    private final BatchJobService batchJobService;

    @Operation(
            summary = "Create multiple jobs",
            description = "Creates multiple asynchronous jobs in a single batch request."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Batch processed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid batch request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @PostMapping
    public ResponseEntity<BatchJobResponse> createBatchJobs(
            @Valid @RequestBody CreateBatchJobRequest request,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        BatchJobResponse response =
                batchJobService.createBatchJobs(
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}