package com.chinmaytech.job_scheduler_backend.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "CreateOrganizationRequest",
        description = "Request payload used to create a new organization"
)
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(
            min = 2,
            max = 150,
            message = "Organization name cannot exceed 150 characters"
    )
    @Schema(
            description = "Name of the organization to be created",
            example = "Chinmay Technologies",
            minLength = 2,
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;
}