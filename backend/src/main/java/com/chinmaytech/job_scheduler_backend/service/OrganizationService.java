package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.dto.organization.CreateOrganizationRequest;
import com.chinmaytech.job_scheduler_backend.dto.organization.OrganizationResponse;
import com.chinmaytech.job_scheduler_backend.entity.Organization;
import com.chinmaytech.job_scheduler_backend.entity.OrganizationMember;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.mapper.OrganizationMappers;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationMemberRepository;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationMappers organizationMapper;
    @Transactional
    public OrganizationResponse createOrganization(
            CreateOrganizationRequest request,
            String userId
    ) {

        if (organizationMemberRepository
                .existsByOwnerAndOrganizationName(
                        userId,
                        request.getName()
                )) {

            throw new ConflictException(
                    "You already have an organization with this name"
            );
        }

        // 1. Create organization
        Organization organization = Organization.builder()
                .name(request.getName())
                .build();

        organization = organizationRepository.save(organization);

        // 2. Make the creator the OWNER
        OrganizationMember member = OrganizationMember.builder()
                .organizationId(organization.getId())
                .userId(userId)
                .role("OWNER")
                .build();

        organizationMemberRepository.save(member);

        // 3. Return response
        return organizationMapper.mapOrganizationToOrganizationResponse(organization);
    }


    public List<OrganizationResponse> getMyOrganizations(String userId) {

        List<OrganizationMember> memberships =
                organizationMemberRepository.findByUserId(userId);

        List<OrganizationResponse> responses = new ArrayList<>();

        for (OrganizationMember member : memberships) {

            Organization organization =
                    organizationRepository
                            .findById(member.getOrganizationId())
                            .orElseThrow();


            responses.add(organizationMapper.mapOrganizationToOrganizationResponse(organization));

        }

        return responses;
    }
}

