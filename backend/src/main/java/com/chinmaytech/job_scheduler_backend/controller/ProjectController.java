package com.chinmaytech.job_scheduler_backend.controller;


import com.chinmaytech.job_scheduler_backend.dto.project.CreateProjectRequest;
import com.chinmaytech.job_scheduler_backend.dto.project.ProjectResponse;
import com.chinmaytech.job_scheduler_backend.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(
        name = "Projects",
        description = "APIs for creating and retrieving projects within organizations"
)
public class ProjectController {

    private final ProjectService projectService;
    @Operation(
            summary = "Create a new project",
            description = "Creates a new project for the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Organization not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Project already exists"
            )
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        ProjectResponse response =
                projectService.createProject(
                        request,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @Operation(
            summary = "Get projects by organization",
            description = "Retrieves all projects belonging to the specified organization for the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Organization not found"
            )
    })
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @Parameter(
                    description = "Unique identifier of the organization",
                    required = true,
                    example = "4e8d6580-228c-4b48-83bb-2de638bd761f"
            )
            @PathVariable String organizationId,
            Authentication authentication
    ) {

        String userId = authentication.getName();

        List<ProjectResponse> projects =
                projectService.getProjects(
                        organizationId,
                        userId
                );

        return ResponseEntity.ok(projects);
    }
}
