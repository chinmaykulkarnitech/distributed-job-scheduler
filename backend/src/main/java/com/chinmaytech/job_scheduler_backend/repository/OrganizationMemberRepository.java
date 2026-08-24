package com.chinmaytech.job_scheduler_backend.repository;

import com.chinmaytech.job_scheduler_backend.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationMemberRepository
        extends JpaRepository<OrganizationMember, String> {
    boolean existsByOrganizationIdAndUserId(
            String organizationId,
            String userId
    );

    @Query("""
    SELECT COUNT(m) > 0
    FROM OrganizationMember m
    JOIN Organization o ON o.id = m.organizationId
    WHERE m.userId = :userId
    AND m.role = 'OWNER'
    AND o.name = :name
""")
    boolean existsByOwnerAndOrganizationName(
            @Param("userId") String userId,
            @Param("name") String name
    );
    List<OrganizationMember> findByUserId(String userId);

    List<OrganizationMember> findByOrganizationId(String organizationId);

}