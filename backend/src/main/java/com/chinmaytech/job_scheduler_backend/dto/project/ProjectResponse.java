package com.chinmaytech.job_scheduler_backend.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(
        name = "ProjectResponse",
        description = "Response representing a project within an organization"
)
public class ProjectResponse {

    @Schema(
            description = "Unique identifier of the project",
            example = "4f939133-d88f-4d51-96a3-834def8a32ee"
    )
    private String id;

    @Schema(
            description = "Unique identifier of the organization that owns the project",
            example = "4e8d6580-228c-4b48-83bb-2de638bd761f"
    )
    private String organizationId;

    @Schema(
            description = "Name of the project",
            example = "Distributed Job Scheduler"
    )
    private String name;

    @Schema(
            description = "Description of the project",
            example = "Production-inspired distributed asynchronous job scheduling platform"
    )
    private String description;

    @Schema(
            description = "Date and time when the project was created",
            example = "2026-08-24T03:10:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the project was last updated",
            example = "2026-08-24T03:15:00"
    )
    private LocalDateTime updatedAt;
}