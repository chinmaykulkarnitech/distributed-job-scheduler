package com.chinmaytech.job_scheduler_backend.service;
//
//
//import com.chinmaytech.job_scheduler_backend.dto.Job.CreateJobRequest;
//import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
//import com.chinmaytech.job_scheduler_backend.entity.Job;
//import com.chinmaytech.job_scheduler_backend.entity.Queue;
//import com.chinmaytech.job_scheduler_backend.mapper.JobMapper;
//import com.chinmaytech.job_scheduler_backend.repository.JobRepository;
//import com.chinmaytech.job_scheduler_backend.repository.QueueRepository;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class JobService {
//
//    private final JobRepository jobRepository;
//    private final QueueRepository queueRepository;
//    private final JobMapper jobMapper;
//
//    @Transactional
//    public JobResponse createJob(CreateJobRequest request, String userId) {
//
//        // 1. Check if queue exists
//        Queue queue = queueRepository
//                .findById(request.getQueueId())
//                .orElseThrow(() ->
//                        new RuntimeException("Queue not found")
//                );
//
//        // 2. Check idempotency key
//        if (request.getIdempotencyKey() != null &&
//                jobRepository.existsByQueueIdAndIdempotencyKey(
//                        request.getQueueId(),
//                        request.getIdempotencyKey()
//                )) {
//
//            throw new RuntimeException(
//                    "Job with this idempotency key already exists"
//            );
//        }
//
//        // 3. Create Job
//        Job job = Job.builder()
//                .queueId(queue.getId())
//                .jobType(request.getJobType())
//                .payload(request.getPayload())
//                .priority(
//                        request.getPriority() != null
//                                ? request.getPriority()
//                                : queue.getPriority()
//                )
//                .runAt(request.getRunAt())
//                .idempotencyKey(request.getIdempotencyKey())
//                .build();
//
//        // 4. Save
//        job = jobRepository.save(job);
//
//        // 5. Return response
//        return jobMapper.mapJobToJobResponse(job);
//    }
//
//    public JobResponse getJob(String jobId, String userId) {
//
//        Job job = jobRepository.findById(jobId)
//                .orElseThrow(() ->
//                        new RuntimeException("Job not found")
//                );
//
//        return jobMapper.mapJobToJobResponse(job);
//    }
//    public List<JobResponse> getJobsByQueue(String queueId, String userId) {
//
//        List<Job> jobs = jobRepository.findByQueueId(queueId);
//
//        List<JobResponse> responses = new java.util.ArrayList<>();
//
//        for (Job job : jobs) {
//            responses.add(
//                    jobMapper.mapJobToJobResponse(job)
//            );
//        }
//
//        return responses;
//    }
//}
//
import com.chinmaytech.job_scheduler_backend.dto.Job.BatchJobResponse;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateBatchJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.CreateJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
import com.chinmaytech.job_scheduler_backend.entity.*;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.exception.ResourceNotFoundException;
import com.chinmaytech.job_scheduler_backend.mapper.JobMapper;
import com.chinmaytech.job_scheduler_backend.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final QueueRepository queueRepository;
    private final ProjectRepository projectRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final RetryPolicyRepository retryPolicyRepository;
    private final WorkerRepository workerRepository;
    private final JobMapper jobMapper;
    @Transactional
    public JobResponse createJob(
            CreateJobRequest request,
            String userId
    ) {

        // 1. Find queue
        Queue queue = queueRepository
                .findById(request.getQueueId())
                .orElseThrow(() ->
        new ResourceNotFoundException("Queue not found")
                );


        // 2. Find project
        Project project = projectRepository
                .findById(queue.getProjectId())
                .orElseThrow(() ->
              new ResourceNotFoundException("Project not found")
                );


        // 3. Check user belongs to organization
        boolean isMember =
                organizationMemberRepository
                        .existsByOrganizationIdAndUserId(
                                project.getOrganizationId(),
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not a member of this organization"
            );
        }


        // 4. Check idempotency
        if (request.getIdempotencyKey() != null &&
                jobRepository.existsByQueueIdAndIdempotencyKey(
                        request.getQueueId(),
                        request.getIdempotencyKey()
                )) {

            throw new ConflictException(
                    "Job with this idempotency key already exists"
            );
        }


        // 5. Create job
        Job job = Job.builder()
                .queueId(queue.getId())
                .jobType(request.getJobType())
                .payload(request.getPayload())
                .priority(
                        request.getPriority() != null
                                ? request.getPriority()
                                : queue.getPriority()
                )
                .runAt(
                        request.getRunAt() != null
                                ? request.getRunAt()
                                : LocalDateTime.now()
                )
                .idempotencyKey(request.getIdempotencyKey())
                .build();


        // 6. Save job
        job = jobRepository.save(job);


        // 7. Return response
        return jobMapper.mapJobToJobResponse(job);
    }


    public JobResponse getJob(
            String jobId,
            String userId
    ) {

        // 1. Find job
        Job job = jobRepository
                .findById(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found")
                );


        // 2. Find queue
        Queue queue = queueRepository
                .findById(job.getQueueId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Queue not found")

                );


        // 3. Find project
        Project project = projectRepository
                .findById(queue.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found")
                );


        // 4. Check organization membership
        boolean isMember =
                organizationMemberRepository
                        .existsByOrganizationIdAndUserId(
                                project.getOrganizationId(),
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not a member of this organization"
            );
        }


        // 5. Return job
        return jobMapper.mapJobToJobResponse(job);
    }


    public List<JobResponse> getJobsByQueue(
            String queueId,
            String userId
    ) {

        // 1. Find queue
        Queue queue = queueRepository
                .findById(queueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Queue not found")

                );


        // 2. Find project
        Project project = projectRepository
                .findById(queue.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found")

                );


        // 3. Check organization membership
        boolean isMember =
                organizationMemberRepository
                        .existsByOrganizationIdAndUserId(
                                project.getOrganizationId(),
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "You are not a member of this organization"
            );
        }


        // 4. Get jobs
        List<Job> jobs =
                jobRepository.findByQueueId(queueId);


        // 5. Convert jobs to responses
        List<JobResponse> responses =
                new java.util.ArrayList<>();

        for (Job job : jobs) {

            responses.add(
                    jobMapper.mapJobToJobResponse(job)
            );
        }


        return responses;
    }
    @Transactional
    public JobResponse claimJob(String workerId) {

        // 1. Find worker
        Worker worker = workerRepository.findByIdForUpdate(workerId)
                .orElseThrow(() ->
                        new ConflictException("Worker is not online")
                );

        // 2. Check worker is online
        if (!"ONLINE".equals(worker.getStatus())) {
            throw new ConflictException(
                    "Worker is not online"
            );
        }

        // 3. Check concurrency
        if (worker.getActiveJobs() >= worker.getConcurrencyLimit()) {
            throw new ConflictException("Worker concurrency limit reached");
        }

        // 4. Find available job
        Job job = jobRepository
                .findNextAvailableJob(LocalDateTime.now())
                .orElseThrow(() ->
                        new ResourceNotFoundException("No jobs available")
                );
        // 5. Claim job
        job.setStatus("RUNNING");
        job.setClaimedBy(workerId);
        job.setClaimedAt(LocalDateTime.now());

        // 6. Increase attempts
        job.setAttemptCount(
                job.getAttemptCount() + 1
        );

        // 7. Increase active jobs
        worker.setActiveJobs(
                worker.getActiveJobs() + 1
        );

        // 8. Save
        jobRepository.save(job);
        workerRepository.save(worker);

        return jobMapper.mapJobToJobResponse(job);
    }
    @Transactional
    public JobResponse completeJob(
            String jobId,
            String workerId
    ) {

        // 1. Find job
        Job job = jobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found")

                );

        // 2. Verify worker owns the job
        if (job.getClaimedBy() == null ||
                !job.getClaimedBy().trim().equals(workerId.trim())) {
            throw new ConflictException(
                    "Worker is not allowed to complete this job. " +
                            "claimedBy=" + job.getClaimedBy() +
                            ", workerId=" + workerId
            );

        }

        // 3. Job must be RUNNING
        if (!"RUNNING".equals(job.getStatus())) {
            throw new ConflictException("Job is not running");
        }

        // 4. Find worker
        Worker worker = workerRepository.findByIdForUpdate(workerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Worker not found")
                );

        // 5. Mark job completed
        job.setStatus("COMPLETED");
        // Worker no longer owns the job
        job.setClaimedBy(null);
        job.setClaimedAt(null);

        if (worker.getActiveJobs() > 0) {
            worker.setActiveJobs(worker.getActiveJobs() - 1);
        }

        // 7. Save
        jobRepository.save(job);
        workerRepository.save(worker);

        // 8. Return
        return jobMapper.mapJobToJobResponse(job);
    }


    @Transactional
    public JobResponse failJob(
            String jobId,
            String workerId
    ) {

        // 1. Find job
        Job job = jobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found")

                );
        Worker worker = workerRepository.findByIdForUpdate(workerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Worker not found")
                );

        // 2. Verify worker owns the job
        if (job.getClaimedBy() == null ||
                !job.getClaimedBy().trim().equals(workerId.trim())) {

            throw new ConflictException(
                    "Worker is not allowed to complete this job. " +
                            "claimedBy=" + job.getClaimedBy() +
                            ", workerId=" + workerId
            );
        }
        // 3. Job must be RUNNING
        if (!"RUNNING".equals(job.getStatus())) {
            throw new ConflictException("Job is not running");
        }

        // 4. Find queue
        Queue queue = queueRepository.findById(job.getQueueId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Queue not found")

                );

        // 5. Find retry policy
        RetryPolicy retryPolicy = null;

        if (queue.getRetryPolicyId() != null) {
            retryPolicy = retryPolicyRepository
                    .findById(queue.getRetryPolicyId())
                    .orElse(null);
        }


        // 6. No retry policy → permanently fail
        if (retryPolicy == null) {

            job.setStatus("FAILED");

            job.setClaimedBy(null);
            job.setClaimedAt(null);

            if (worker.getActiveJobs() > 0) {
                worker.setActiveJobs(
                        worker.getActiveJobs() - 1
                );
            }

            workerRepository.save(worker);
            job = jobRepository.save(job);

            return jobMapper.mapJobToJobResponse(job);
        }

        // 7. Check maximum attempts
        if (job.getAttemptCount() >= retryPolicy.getMaxAttempts()) {

            job.setStatus("FAILED");

            job.setClaimedBy(null);
            job.setClaimedAt(null);

            if (worker.getActiveJobs() > 0) {
                worker.setActiveJobs(
                        worker.getActiveJobs() - 1
                );
            }

            workerRepository.save(worker);
            job = jobRepository.save(job);

            return jobMapper.mapJobToJobResponse(job);
        }
        // 8. Calculate retry delay
        long delaySeconds;

        if ("EXPONENTIAL".equalsIgnoreCase(
                retryPolicy.getStrategy())) {

            delaySeconds =
                    (long) retryPolicy.getInitialDelaySeconds()
                            * (1L << (job.getAttemptCount() - 1));

        } else {

            // FIXED strategy
            delaySeconds =
                    retryPolicy.getInitialDelaySeconds();
        }

        // 9. Respect maximum delay
        delaySeconds = Math.min(
                delaySeconds,
                retryPolicy.getMaxDelaySeconds()
        );

        // 10. Schedule retry
        job.setStatus("QUEUED");

        job.setRunAt(
                LocalDateTime.now()
                        .plusSeconds(delaySeconds)
        );

        // Worker is no longer holding the job
        job.setClaimedBy(null);
        job.setClaimedAt(null);
        if (worker.getActiveJobs() > 0) {
            worker.setActiveJobs(
                    worker.getActiveJobs() - 1
            );
        }

        workerRepository.save(worker);
        // 11. Save
        job = jobRepository.save(job);

        // 12. Return
        return jobMapper.mapJobToJobResponse(job);
    }


    public List<JobResponse> getAllJobs(String userId) {

        List<Job> jobs =
                jobRepository.findAllByOrderByCreatedAtDesc();

        List<JobResponse> responses =
                new java.util.ArrayList<>();

        for (Job job : jobs) {

            Queue queue = queueRepository
                    .findById(job.getQueueId())
                    .orElse(null);

            if (queue == null) {
                continue;
            }

            Project project = projectRepository
                    .findById(queue.getProjectId())
                    .orElse(null);

            if (project == null) {
                continue;
            }

            boolean isMember =
                    organizationMemberRepository
                            .existsByOrganizationIdAndUserId(
                                    project.getOrganizationId(),
                                    userId
                            );

            if (!isMember) {
                continue;
            }

            responses.add(
                    jobMapper.mapJobToJobResponse(job)
            );
        }

        return responses;
    }


    @Transactional
    public BatchJobResponse createBatchJobs(
            CreateBatchJobRequest request,
            String userId
    ) {

        List<JobResponse> responses = new java.util.ArrayList<>();

        for (CreateJobRequest jobRequest : request.getJobs()) {

            JobResponse response =
                    createJob(jobRequest, userId);

            responses.add(response);
        }

        return BatchJobResponse.builder()
                .totalJobs(responses.size())
                .jobs(responses)
                .build();
    }
}