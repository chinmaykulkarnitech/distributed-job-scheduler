package com.chinmaytech.job_scheduler_backend.controller;

import com.chinmaytech.job_scheduler_backend.entity.RetryPolicy;
import com.chinmaytech.job_scheduler_backend.service.RetryPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retry-policies")
@RequiredArgsConstructor
@Tag(
        name = "Retry Policies",
        description = "APIs for managing retry policies used to retry failed jobs"
)
public class RetryPolicyController {

    private final RetryPolicyService retryPolicyService;


    // CREATE
    // POST /api/retry-policies
    @Operation(
            summary = "Create a retry policy",
            description = "Creates a new retry policy that defines how failed jobs should be retried."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Retry policy created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid retry policy data"
            )
    })
    @PostMapping
    public ResponseEntity<RetryPolicy> createRetryPolicy(
            @RequestBody RetryPolicy retryPolicy
    ) {

        RetryPolicy response =
                retryPolicyService.createRetryPolicy(
                        retryPolicy
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // GET ALL
    // GET /api/retry-policies
    @Operation(
            summary = "Get all retry policies",
            description = "Retrieves all retry policies configured in the scheduler."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retry policies retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<List<RetryPolicy>> getAllRetryPolicies() {

        return ResponseEntity.ok(
                retryPolicyService.getAllRetryPolicies()
        );
    }


    // GET ONE
    // GET /api/retry-policies/{id}

    @Operation(
            summary = "Get retry policy by ID",
            description = "Retrieves a specific retry policy using its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retry policy retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Retry policy not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RetryPolicy> getRetryPolicy(
            @Parameter(
                    description = "Unique identifier of the retry policy",
                    required = true,
                    example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
            )
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                retryPolicyService.getRetryPolicy(id)
        );
    }


    // UPDATE
    // PUT /api/retry-policies/{id}
    @Operation(
            summary = "Update a retry policy",
            description = "Updates the configuration of an existing retry policy."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Retry policy updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid retry policy data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Retry policy not found"
            )
    })



    @PutMapping("/{id}")
    public ResponseEntity<RetryPolicy> updateRetryPolicy(
            @Parameter(
                    description = "Unique identifier of the retry policy",
                    required = true,
                    example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
            )
            @PathVariable String id,
            @RequestBody RetryPolicy retryPolicy
    ) {

        return ResponseEntity.ok(
                retryPolicyService.updateRetryPolicy(
                        id,
                        retryPolicy
                )
        );
    }


    // DELETE
    // DELETE /api/retry-policies/{id}
    @Operation(
            summary = "Delete a retry policy",
            description = "Deletes an existing retry policy."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Retry policy deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Retry policy not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRetryPolicy(
            @Parameter(
                    description = "Unique identifier of the retry policy",
                    required = true,
                    example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
            )
            @PathVariable String id
    ) {

        retryPolicyService.deleteRetryPolicy(id);

        return ResponseEntity.noContent().build();
    }
}