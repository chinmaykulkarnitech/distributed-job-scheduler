package com.chinmaytech.job_scheduler_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(
        name = "RegisterResponse",
        description = "Response returned after successfully registering a new user"
)
public class RegisterResponse {

    @Schema(
            description = "Unique identifier assigned to the newly registered user",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
    )
    private String id;

    @Schema(
            description = "Full name of the registered user",
            example = "Chinmay Kulkarni"
    )
    private String name;

    @Schema(
            description = "Email address of the registered user",
            example = "chinmay@example.com"
    )
    private String email;

    @Schema(
            description = "Date and time when the user account was created",
            example = "2026-08-24T03:30:00"
    )
    private LocalDateTime createdAt;
}