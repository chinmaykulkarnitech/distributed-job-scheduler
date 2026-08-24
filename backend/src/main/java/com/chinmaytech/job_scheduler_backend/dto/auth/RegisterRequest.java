package com.chinmaytech.job_scheduler_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "RegisterRequest",
        description = "Request payload used to create a new user account"
)
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(
            max = 100,
            message = "Name must not exceed 100 characters"
    )
    @Schema(
            description = "Full name of the user",
            example = "Chinmay Kulkarni",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;


    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(
            max = 255,
            message = "Email must not exceed 255 characters"
    )
    @Schema(
            description = "Unique email address used for authentication",
            example = "chinmay@example.com",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;


    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters"
    )
    @Schema(
            description = "Password for the new account. Must contain between 8 and 100 characters",
            example = "Password@123",
            minLength = 8,
            maxLength = 100,
            format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}