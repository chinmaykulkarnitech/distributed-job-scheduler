package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.entity.Worker;
import com.chinmaytech.job_scheduler_backend.entity.WorkerHeartbeat;
import com.chinmaytech.job_scheduler_backend.repository.WorkerHeartbeatRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private WorkerHeartbeatRepository workerHeartbeatRepository;

    @InjectMocks
    private WorkerService workerService;

    @Test
    void sendHeartbeat_shouldUpdateWorkerAndStoreHeartbeat() {

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
                1
        );

        assertSame(savedHeartbeat, result);

        verify(worker).setStatus("ONLINE");
        verify(worker).setActiveJobs(1);
        verify(worker).setLastHeartbeatAt(any());

        verify(workerRepository).save(worker);
        verify(workerHeartbeatRepository)
                .save(any(WorkerHeartbeat.class));
    }
}