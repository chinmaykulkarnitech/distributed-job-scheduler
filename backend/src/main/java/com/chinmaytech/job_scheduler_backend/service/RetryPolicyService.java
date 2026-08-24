package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.entity.RetryPolicy;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.exception.ResourceNotFoundException;
import com.chinmaytech.job_scheduler_backend.repository.RetryPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetryPolicyService {

    private final RetryPolicyRepository retryPolicyRepository;

    // CREATE
    public RetryPolicy createRetryPolicy(RetryPolicy retryPolicy) {

        if (retryPolicyRepository.existsByName(
                retryPolicy.getName())) {

            throw new ConflictException(
                    "Retry policy with this name already exists"
            );
        }

        return retryPolicyRepository.save(retryPolicy);
    }

    // GET BY ID
    public RetryPolicy getRetryPolicy(String id) {

        return retryPolicyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Retry policy not found"
                        )
                );
    }

    // GET ALL
    public List<RetryPolicy> getAllRetryPolicies() {

        return retryPolicyRepository.findAll();
    }

    // UPDATE
    public RetryPolicy updateRetryPolicy(
            String id,
            RetryPolicy request
    ) {

        RetryPolicy existing =
                retryPolicyRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Retry policy not found"
                                )
                        );

        existing.setName(request.getName());
        existing.setStrategy(request.getStrategy());
        existing.setMaxAttempts(
                request.getMaxAttempts()
        );
        existing.setInitialDelaySeconds(
                request.getInitialDelaySeconds()
        );
        existing.setMaxDelaySeconds(
                request.getMaxDelaySeconds()
        );

        return retryPolicyRepository.save(existing);
    }

    // DELETE
    public void deleteRetryPolicy(String id) {

        RetryPolicy existing =
                retryPolicyRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Retry policy not found"
                                )
                        );

        retryPolicyRepository.delete(existing);
    }
}