package com.chinmaytech.job_scheduler_backend.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "CreateProjectRequest",
        description = "Request payload used to create a new project within an organization"
)
public class CreateProjectRequest {

    @NotNull(message = "Organization ID is required")
    @Schema(
            description = "Unique identifier of the organization to which the project belongs",
            example = "4e8d6580-228c-4b48-83bb-2de638bd761f",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String organizationId;


    @NotBlank(message = "Project name is required")
    @Size(
            max = 150,
            message = "Project name must not exceed 150 characters"
    )
    @Schema(
            description = "Name of the project",
            example = "Distributed Job Scheduler",
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;


    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    @Schema(
            description = "Optional description explaining the purpose or scope of the project",
            example = "Production-inspired distributed asynchronous job scheduling platform",
            maxLength = 1000
    )
    private String description;
}