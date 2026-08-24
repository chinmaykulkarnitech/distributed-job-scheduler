package com.chinmaytech.job_scheduler_backend.repository;

import com.chinmaytech.job_scheduler_backend.entity.WorkerHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerHeartbeatRepository
        extends JpaRepository<WorkerHeartbeat, String> {

    List<WorkerHeartbeat> findByWorkerIdOrderByHeartbeatAtDesc(
            String workerId
    );
}