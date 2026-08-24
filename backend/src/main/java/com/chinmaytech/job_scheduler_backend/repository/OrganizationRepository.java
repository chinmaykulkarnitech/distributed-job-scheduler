package com.chinmaytech.job_scheduler_backend.repository;

import com.chinmaytech.job_scheduler_backend.controller.OrganizationController;
import com.chinmaytech.job_scheduler_backend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrganizationRepository extends JpaRepository<Organization,String> {
    boolean existsByName(String name);
}
