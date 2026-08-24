package com.chinmaytech.job_scheduler_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
        name = "LoginResponse",
        description = "Response returned after successful user authentication"
)
public class LoginResponse {

    @Schema(
            description = "JWT access token used to authenticate protected API requests",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Authentication token type",
            example = "Bearer"
    )
    private String tokenType;

    @Schema(
            description = "Unique identifier of the authenticated user",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
    )
    private String userId;

    @Schema(
            description = "Name of the authenticated user",
            example = "Chinmay Kulkarni"
    )
    private String name;

    @Schema(
            description = "Email address of the authenticated user",
            example = "chinmay@example.com"
    )
    private String email;
}