package com.chinmaytech.job_scheduler_backend.mapper;

import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
import com.chinmaytech.job_scheduler_backend.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobResponse mapJobToJobResponse(Job job) {

        return JobResponse.builder()
                .id(job.getId())
                .queueId(job.getQueueId())
                .jobType(job.getJobType())
                .status(job.getStatus())
                .priority(job.getPriority())
                .payload(job.getPayload())
                .runAt(job.getRunAt())
                .attemptCount(job.getAttemptCount())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .idempotencyKey(job.getIdempotencyKey())
                .claimedBy(job.getClaimedBy())
                .claimedAt(job.getClaimedAt())
                .build();
    }
}

