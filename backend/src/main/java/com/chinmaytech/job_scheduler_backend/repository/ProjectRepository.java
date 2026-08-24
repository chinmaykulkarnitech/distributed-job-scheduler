package com.chinmaytech.job_scheduler_backend.repository;

import com.chinmaytech.job_scheduler_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    List<Project> findByOrganizationId(String organizationId);

    boolean existsByOrganizationIdAndName(
            String organizationId,
            String name
    );
}
