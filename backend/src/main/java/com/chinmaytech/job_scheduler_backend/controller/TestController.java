package com.chinmaytech.job_scheduler_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(
        name = "Authentication Test",
        description = "Endpoints used to verify authenticated access"
)
public class TestController {
    @Operation(
            summary = "Verify authenticated user",
            description = "Returns the username or user ID of the currently authenticated user. " +
                    "This endpoint can be used to verify that JWT authentication is working correctly."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User is authenticated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or JWT token is invalid"
            )
    })
    @GetMapping("/test")
    public String test(Authentication authentication) {

        return "Authenticated user: "
                + authentication.getName();
    }
}