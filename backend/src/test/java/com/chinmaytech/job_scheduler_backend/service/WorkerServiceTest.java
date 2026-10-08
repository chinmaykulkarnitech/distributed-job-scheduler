package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.entity.Worker;
import com.chinmaytech.job_scheduler_backend.entity.WorkerHeartbeat;
import com.chinmaytech.job_scheduler_backend.repository.OrganizationMemberRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerHeartbeatRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerHeartbeatRepository workerHeartbeatRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @InjectMocks
    private WorkerService workerService;

    @Test
    void sendHeartbeat_shouldUpdateLivenessButNotTrustReportedActiveJobs() {

        String workerId = "worker-1";

        Worker worker = mock(Worker.class);
        WorkerHeartbeat savedHeartbeat = mock(WorkerHeartbeat.class);

        when(workerRepository.findById(workerId))
                .thenReturn(Optional.of(worker));

        when(workerHeartbeatRepository.save(any(WorkerHeartbeat.class)))
                .thenReturn(savedHeartbeat);

        WorkerHeartbeat result = workerService.sendHeartbeat(
                workerId,
                new BigDecimal("25.50"),
                new BigDecimal("40.25"),
                7
        );

        assertSame(savedHeartbeat, result);

        verify(worker).setStatus("ONLINE");
        verify(worker).setLastHeartbeatAt(any());

        // The worker-reported value must never overwrite the server's own count.
        verify(worker, never()).setActiveJobs(any());

        // ...but it is still recorded in the heartbeat history row.
        ArgumentCaptor<WorkerHeartbeat> captor =
                ArgumentCaptor.forClass(WorkerHeartbeat.class);
        verify(workerHeartbeatRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getActiveJobs());

        verify(workerRepository).save(worker);
    }

    @Test
    void registerWorker_shouldAssignOrganizationWhenCallerIsMember() {

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId("org-1", "user-1"))
                .thenReturn(true);

        when(workerRepository.save(any(Worker.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Worker worker = workerService.registerWorker("w1", "org-1", "user-1");

        assertEquals("org-1", worker.getOrganizationId());
        assertEquals("ONLINE", worker.getStatus());
    }

    @Test
    void registerWorker_shouldRejectNonMember() {

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId("org-1", "intruder"))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> workerService.registerWorker("w1", "org-1", "intruder")
        );

        verify(workerRepository, never()).save(any(Worker.class));
    }

    @Test
    void getWorker_shouldRejectUserOutsideWorkersOrganization() {

        Worker worker = mock(Worker.class);

        when(workerRepository.findById("worker-1"))
                .thenReturn(Optional.of(worker));
        when(worker.getOrganizationId()).thenReturn("org-A");
        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId("org-A", "user-in-B"))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> workerService.getWorker("worker-1", "user-in-B")
        );
    }

    @Test
    void getOnlineWorkers_shouldRequireMembershipAndFilterByOrganization() {

        Worker worker = mock(Worker.class);

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId("org-A", "user-1"))
                .thenReturn(true);
        when(workerRepository.findByStatusAndOrganizationId("ONLINE", "org-A"))
                .thenReturn(List.of(worker));

        List<Worker> result = workerService.getOnlineWorkers("org-A", "user-1");

        assertEquals(1, result.size());
        verify(workerRepository, never()).findByStatus(any());
    }

    @Test
    void getOnlineWorkers_shouldRejectNonMember() {

        when(organizationMemberRepository
                .existsByOrganizationIdAndUserId("org-A", "user-in-B"))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> workerService.getOnlineWorkers("org-A", "user-in-B")
        );
    }
}
