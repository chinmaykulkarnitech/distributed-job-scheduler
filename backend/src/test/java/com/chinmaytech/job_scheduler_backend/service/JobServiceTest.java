package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.dto.Job.CreateJobRequest;
import com.chinmaytech.job_scheduler_backend.dto.Job.JobResponse;
import com.chinmaytech.job_scheduler_backend.entity.Job;
import com.chinmaytech.job_scheduler_backend.entity.Project;
import com.chinmaytech.job_scheduler_backend.entity.Queue;
import com.chinmaytech.job_scheduler_backend.entity.RetryPolicy;
import com.chinmaytech.job_scheduler_backend.entity.Worker;
import com.chinmaytech.job_scheduler_backend.exception.ConflictException;
import com.chinmaytech.job_scheduler_backend.mapper.JobMapper;
import com.chinmaytech.job_scheduler_backend.repository.JobRepository;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationMemberRepository;
import com.chinmaytech.job_scheduler_backend.repository.ProjectRepository;
import com.chinmaytech.job_scheduler_backend.repository.QueueRepository;
import com.chinmaytech.job_scheduler_backend.repository.RetryPolicyRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private RetryPolicyRepository retryPolicyRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    private final String userId = "user-1";
    private final String queueId = "queue-1";
    private final String projectId = "project-1";
    private final String organizationId = "org-1";
    private final String workerId = "worker-1";

    @BeforeEach
    void setUp() {
        reset(
                jobRepository,
                queueRepository,
                projectRepository,
                organizationMemberRepository,
                retryPolicyRepository,
                workerRepository,
                jobMapper
        );
    }

    @Test
    void createJob_shouldCreateJobSuccessfully() {

        Queue queue = mock(Queue.class);
        Project project = mock(Project.class);

        when(queueRepository.findById(queueId))
                .thenReturn(Optional.of(queue));

        when(queue.getId())
                .thenReturn(queueId);

        when(queue.getProjectId())
                .thenReturn(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(project.getOrganizationId())
                .thenReturn(organizationId);

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(true);

        when(jobRepository
                .existsByQueueIdAndIdempotencyKey(queueId, "job-001"))
                .thenReturn(false);

        CreateJobRequest request = mock(CreateJobRequest.class);

        when(request.getQueueId())
                .thenReturn(queueId);

        when(request.getJobType())
                .thenReturn("EMAIL");

        when(request.getPayload())
                .thenReturn("{\"message\":\"hello\"}");

        when(request.getPriority())
                .thenReturn(5);

        when(request.getRunAt())
                .thenReturn(LocalDateTime.now());

        when(request.getIdempotencyKey())
                .thenReturn("job-001");

        Job savedJob = Job.builder()
                .queueId(queueId)
                .jobType("EMAIL")
                .payload("{\"message\":\"hello\"}")
                .priority(5)
                .runAt(LocalDateTime.now())
                .idempotencyKey("job-001")
                .build();

        when(jobRepository.save(any(Job.class)))
                .thenReturn(savedJob);

        JobResponse expectedResponse = mock(JobResponse.class);

        when(jobMapper.mapJobToJobResponse(savedJob))
                .thenReturn(expectedResponse);

        JobResponse result =
                jobService.createJob(request, userId);

        assertSame(expectedResponse, result);

        verify(jobRepository)
                .save(any(Job.class));

        verify(jobMapper)
                .mapJobToJobResponse(savedJob);
    }

    @Test
    void createJob_shouldRejectDuplicateIdempotencyKey() {

        Queue queue = mock(Queue.class);
        Project project = mock(Project.class);

        when(queueRepository.findById(queueId))
                .thenReturn(Optional.of(queue));

        when(queue.getProjectId())
                .thenReturn(projectId);

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(project.getOrganizationId())
                .thenReturn(organizationId);

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId(organizationId, userId))
                .thenReturn(true);

        when(jobRepository
                .existsByQueueIdAndIdempotencyKey(
                        queueId,
                        "duplicate-key"
                ))
                .thenReturn(true);

        CreateJobRequest request = mock(CreateJobRequest.class);

        when(request.getQueueId())
                .thenReturn(queueId);

        when(request.getIdempotencyKey())
                .thenReturn("duplicate-key");

        assertThrows(
                ConflictException.class,
                () -> jobService.createJob(request, userId)
        );

        verify(jobRepository, never())
                .save(any(Job.class));
    }

    @Test
    void claimJob_shouldSetRunningAndIncrementWorkerActiveJobs() {

        Worker worker = mock(Worker.class);
        Job job = mock(Job.class);

        when(workerRepository.findByIdForUpdate(workerId))
                .thenReturn(Optional.of(worker));

        when(worker.getStatus())
                .thenReturn("ONLINE");

        when(worker.getActiveJobs())
                .thenReturn(0);

        when(worker.getConcurrencyLimit())
                .thenReturn(2);

        when(jobRepository
                .findNextAvailableJob(any(LocalDateTime.class)))
                .thenReturn(Optional.of(job));

        when(job.getAttemptCount())
                .thenReturn(0);

        JobResponse response = mock(JobResponse.class);

        when(jobMapper.mapJobToJobResponse(job))
                .thenReturn(response);

        JobResponse result =
                jobService.claimJob(workerId);

        assertSame(response, result);

        verify(job)
                .setStatus("RUNNING");

        verify(job)
                .setClaimedBy(workerId);

        verify(job)
                .setClaimedAt(any(LocalDateTime.class));

        verify(job)
                .setAttemptCount(1);

        verify(worker)
                .setActiveJobs(1);

        verify(jobRepository)
                .save(job);

        verify(workerRepository)
                .save(worker);
    }

    @Test
    void completeJob_shouldMarkJobCompletedAndReleaseWorker() {

        Job job = mock(Job.class);
        Worker worker = mock(Worker.class);

        when(jobRepository.findByIdForUpdate("job-1"))
                .thenReturn(Optional.of(job));

        when(job.getClaimedBy())
                .thenReturn(workerId);

        when(job.getStatus())
                .thenReturn("RUNNING");

        when(workerRepository.findByIdForUpdate(workerId))
                .thenReturn(Optional.of(worker));

        when(worker.getActiveJobs())
                .thenReturn(1);

        JobResponse response = mock(JobResponse.class);

        when(jobMapper.mapJobToJobResponse(job))
                .thenReturn(response);

        JobResponse result =
                jobService.completeJob("job-1", workerId);

        assertSame(response, result);

        verify(job)
                .setStatus("COMPLETED");

        verify(job)
                .setClaimedBy(null);

        verify(job)
                .setClaimedAt(null);

        verify(worker)
                .setActiveJobs(0);

        verify(jobRepository)
                .save(job);

        verify(workerRepository)
                .save(worker);
    }

    @Test
    void failJob_shouldQueueJobForRetryWhenAttemptsRemain() {

        Job job = mock(Job.class);
        Worker worker = mock(Worker.class);
        Queue queue = mock(Queue.class);
        RetryPolicy retryPolicy = mock(RetryPolicy.class);

        when(jobRepository.findByIdForUpdate("job-1"))
                .thenReturn(Optional.of(job));

        when(job.getClaimedBy())
                .thenReturn(workerId);

        when(job.getStatus())
                .thenReturn("RUNNING");

        when(job.getQueueId())
                .thenReturn(queueId);

        when(job.getAttemptCount())
                .thenReturn(1);

        when(workerRepository.findByIdForUpdate(workerId))
                .thenReturn(Optional.of(worker));

        when(worker.getActiveJobs())
                .thenReturn(1);

        when(queueRepository.findById(queueId))
                .thenReturn(Optional.of(queue));

        when(queue.getRetryPolicyId())
                .thenReturn("retry-1");

        when(retryPolicyRepository.findById("retry-1"))
                .thenReturn(Optional.of(retryPolicy));

        when(retryPolicy.getMaxAttempts())
                .thenReturn(3);

        when(retryPolicy.getStrategy())
                .thenReturn("FIXED");

        when(retryPolicy.getInitialDelaySeconds())
                .thenReturn(10);

        when(retryPolicy.getMaxDelaySeconds())
                .thenReturn(60);

        JobResponse response = mock(JobResponse.class);

        // Important: failJob assigns the result of save() back to job.
        when(jobRepository.save(any(Job.class)))
                .thenReturn(job);

        when(jobMapper.mapJobToJobResponse(job))
                .thenReturn(response);

        JobResponse result =
                jobService.failJob("job-1", workerId);

        assertSame(response, result);

        verify(job)
                .setStatus("QUEUED");

        verify(job)
                .setClaimedBy(null);

        verify(job)
                .setClaimedAt(null);

        verify(job)
                .setRunAt(any(LocalDateTime.class));

        verify(worker)
                .setActiveJobs(0);

        verify(workerRepository)
                .save(worker);

        verify(jobRepository)
                .save(job);
    }
}