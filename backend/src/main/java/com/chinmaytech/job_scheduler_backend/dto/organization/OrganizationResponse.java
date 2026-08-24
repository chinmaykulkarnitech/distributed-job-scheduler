package com.chinmaytech.job_scheduler_backend.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Schema(
        name = "OrganizationResponse",
        description = "Response representing an organization"
)
public class OrganizationResponse {

    @Schema(
            description = "Unique identifier of the organization",
            example = "4e8d6580-228c-4b48-83bb-2de638bd761f"
    )
    private String id;

    @Schema(
            description = "Name of the organization",
            example = "Chinmay Technologies"
    )
    private String name;

    @Schema(
            description = "Date and time when the organization was created",
            example = "2026-08-24T03:00:00"
    )
    private LocalDateTime createdAt;
}