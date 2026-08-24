package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.dto.Job.BatchJobResponse;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateBatchJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final JobService jobService;

    @Transactional
    public BatchJobResponse createBatchJobs(
            CreateBatchJobRequest request,
            String userId
    ) {

        List<JobResponse> successfulJobs = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (CreateJobRequest jobRequest : request.getJobs()) {

            try {

                JobResponse response =
                        createJob(jobRequest, userId);

                successfulJobs.add(response);

            } catch (RuntimeException e) {

                errors.add(
                        "Job with type '" +
                                jobRequest.getJobType() +
                                "' failed: " +
                                e.getMessage()
                );
            }
        }

        return BatchJobResponse.builder()
                .totalJobs(request.getJobs().size())
                .successfulJobs(successfulJobs.size())
                .failedJobs(errors.size())
                .jobs(successfulJobs)
                .errors(errors)
                .build();
    }

    private JobResponse createJob(
            CreateJobRequest jobRequest,
            String userId
    ) {

        return jobService.createJob(
                jobRequest,
                userId
        );
    }
}