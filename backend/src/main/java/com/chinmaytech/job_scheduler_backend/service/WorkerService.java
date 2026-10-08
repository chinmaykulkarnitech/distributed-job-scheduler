package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.entity.Worker;
import com.chinmaytech.job_scheduler_backend.entity.WorkerHeartbeat;
import com.chinmaytech.job_scheduler_backend.exception.ResourceNotFoundException;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationMemberRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerHeartbeatRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerHeartbeatRepository workerHeartbeatRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Transactional
    public Worker registerWorker(String name, String organizationId, String userId) {

        requireMember(organizationId, userId);

        String hostname;

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "unknown";
        }

        Worker worker = Worker.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .organizationId(organizationId)
                .status("ONLINE")
                .hostname(hostname)
                .lastHeartbeatAt(LocalDateTime.now())
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .concurrencyLimit(1)
                .activeJobs(0)
                .build();

        return workerRepository.save(worker);
    }

    public Worker getWorker(String workerId, String userId) {

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Worker not found")
                );

        requireMember(worker.getOrganizationId(), userId);

        return worker;
    }

    public List<Worker> getOnlineWorkers(String organizationId, String userId) {

        requireMember(organizationId, userId);

        return workerRepository.findByStatusAndOrganizationId("ONLINE", organizationId);
    }

    @Transactional
    public WorkerHeartbeat sendHeartbeat(
            String workerId,
            BigDecimal cpuUsage,
            BigDecimal memoryUsage,
            Integer activeJobs
    ) {

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Worker not found")
                );

        LocalDateTime now = LocalDateTime.now();

        // Update worker liveness only. The server's own count (maintained by
        // claim, complete and fail) is the source of truth for activeJobs, so the
        // worker-reported value is stored in the heartbeat history row only.
        worker.setLastHeartbeatAt(now);
        worker.setStatus("ONLINE");

        workerRepository.save(worker);

        // Store heartbeat history
        WorkerHeartbeat heartbeat = WorkerHeartbeat.builder()
                .workerId(workerId)
                .heartbeatAt(now)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .activeJobs(activeJobs)
                .build();

        return workerHeartbeatRepository.save(heartbeat);
    }

    @Transactional
    public Worker heartbeat(String workerId) {

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Worker not found")
                );

        worker.setStatus("ONLINE");
        worker.setLastHeartbeatAt(LocalDateTime.now());

        return workerRepository.save(worker);
    }

    private void requireMember(String organizationId, String userId) {

        if (organizationId == null
                || !organizationMemberRepository
                        .existsByOrganizationIdAndUserId(organizationId, userId)) {

            throw new AccessDeniedException(
                    "You are not a member of this organization"
            );
        }
    }
}
