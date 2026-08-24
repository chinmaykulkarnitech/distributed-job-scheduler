package com.chinmaytech.job_scheduler_backend.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "organization_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_org_user",
                        columnNames = {"organization_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "OrganizationMember",
        description = "Represents a user's membership and role within an organization"
)
public class OrganizationMember {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the organization membership",
            example = "4e8d6580-228c-4b48-83bb-2de638bd761f"
    )
    private String id;


    @Column(name = "organization_id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the organization",
            example = "576ae9a9-ce17-40da-bd90-a155e4578d73"
    )
    private String organizationId;


    @Column(name = "user_id", length = 36, nullable = false)
    @Schema(
            description = "Unique identifier of the user who belongs to the organization",
            example = "823bc982-3bb9-4d85-a107-b9a7256f02a7"
    )
    private String userId;


    @Column(name = "role", length = 30, nullable = false)
    @Schema(
            description = "Role assigned to the user within the organization",
            example = "OWNER",
            allowableValues = {
                    "OWNER",
                    "ADMIN",
                    "MEMBER"
            }
    )
    private String role;


    @Column(name = "joined_at", nullable = false)
    @Schema(
            description = "Date and time when the user joined the organization",
            example = "2026-08-22T18:34:30"
    )
    private LocalDateTime joinedAt;


    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }
}