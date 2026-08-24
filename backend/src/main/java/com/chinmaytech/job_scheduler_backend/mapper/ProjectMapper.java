package com.chinmaytech.job_scheduler_backend.mapper;
import com.chinmaytech.job_scheduler_backend.dto.project.ProjectResponse;
import com.chinmaytech.job_scheduler_backend.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse mapProjectToProjectResponse(Project project) {

        return ProjectResponse.builder()
                .id(project.getId())
                .organizationId(project.getOrganizationId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
