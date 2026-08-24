package com.chinmaytech.job_scheduler_backend.mapper;

import com.chinmaytech.job_scheduler_backend.dto.organization.OrganizationResponse;
import com.chinmaytech.job_scheduler_backend.entity.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMappers {


    public OrganizationResponse mapOrganizationToOrganizationResponse(Organization savedOrganization) {

        return OrganizationResponse.builder()
                .id(savedOrganization.getId())
                .name(savedOrganization.getName())
                .createdAt(savedOrganization.getCreatedAt())
                .build();
    }

}
