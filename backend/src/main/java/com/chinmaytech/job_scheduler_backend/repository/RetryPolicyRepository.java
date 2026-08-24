package com.chinmaytech.job_scheduler_backend.repository;

import com.chinmaytech.job_scheduler_backend.entity.RetryPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RetryPolicyRepository
        extends JpaRepository<RetryPolicy, String> {

    Optional<RetryPolicy> findByName(String name);

    boolean existsByName(String name);

}