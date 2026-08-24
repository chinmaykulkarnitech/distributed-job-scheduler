package com.chinmaytech.job_scheduler_backend.controller;

import com.chinmaytech.job_scheduler_backend.dto.organization.CreateOrganizationRequest;
import com.chinmaytech.job_scheduler_backend.dto.organization.OrganizationResponse;
import com.chinmaytech.job_scheduler_backend.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(
        name = "Organizations",
        description = "APIs for creating and managing organizations"
)

public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(
            summary = "Create an organization",
            description = "Creates a new organization for the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Organization created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid organization request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Organization already exists"
            )
    })
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            Authentication authentication
    ) {


        String userId = authentication.getName();

        OrganizationResponse response =
                organizationService.createOrganization(
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get my organizations",
            description = "Retrieves all organizations associated with the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Organizations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations(
            Authentication authentication
    ) {

        String userId = authentication.getName();

        List<OrganizationResponse> organizations =
                organizationService.getMyOrganizations(userId);

        return ResponseEntity.ok(organizations);
    }
}