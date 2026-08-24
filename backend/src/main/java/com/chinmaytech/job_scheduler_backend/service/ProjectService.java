package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.dto.project.CreateProjectRequest;
import com.chinmaytech.job_scheduler_backend.dto.project.ProjectResponse;
import com.chinmaytech.job_scheduler_backend.entity.Organization;
import com.chinmaytech.job_scheduler_backend.entity.Project;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.exception.ResourceNotFoundException;
import com.chinmaytech.job_scheduler_backend.mapper.ProjectMapper;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationMemberRepository;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationRepository;
import com.chinmaytech.job_scheduler_backend.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request,
            String userId
    ) {

        // 1. Check organization exists
        Organization organization = organizationRepository
                .findById(request.getOrganizationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Organization not found")
                );

        // 2. Check user belongs to organization
        boolean isMember =
                organizationMemberRepository
                        .existsByOrganizationIdAndUserId(
                                request.getOrganizationId(),
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not a member of this organization"
            );
        }

        // 3. Check duplicate project name
        boolean projectExists =
                projectRepository.existsByOrganizationIdAndName(
                        request.getOrganizationId(),
                        request.getName()
                );

        if (projectExists) {
            throw new ConflictException(
                    "You already have a project with this name"
            );
        }

        // 4. Create project
        Project project = Project.builder()
                .organizationId(organization.getId())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // 5. Save project
        project = projectRepository.save(project);

        // 6. Return response
        return projectMapper.mapProjectToProjectResponse(project);
    }

    public List<ProjectResponse> getProjects(
            String organizationId,
            String userId
    ) {

        // 1. Check user belongs to organization
        boolean isMember =
                organizationMemberRepository
                        .existsByOrganizationIdAndUserId(
                                organizationId,
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not a member of this organization"
            );
        }

        // 2. Get projects
        List<Project> projects =
                projectRepository.findByOrganizationId(
                        organizationId
                );

        // 3. Convert to response
        List<ProjectResponse> responses =
                new java.util.ArrayList<>();

        for (Project project : projects) {

            responses.add(
                    projectMapper.mapProjectToProjectResponse(
                            project
                    )
            );
        }

        return responses;
    }
}
