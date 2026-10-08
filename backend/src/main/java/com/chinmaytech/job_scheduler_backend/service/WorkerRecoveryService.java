package com.chinmaytech.job_scheduler_backend.service;

import com.chinmaytech.job_scheduler_backend.entity.Job;
import com.chinmaytech.job_scheduler_backend.entity.Worker;
import com.chinmaytech.job_scheduler_backend.repository.JobRepository;
import com.chinmaytech.job_scheduler_backend.repository.WorkerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerRecoveryService {

    private final WorkerRepository workerRepository;
    private final JobRepository jobRepository;

    /*
     * Worker is considered stale if
     * it has not sent heartbeat for 30 seconds.
     */
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 60;

    /*
     * Run recovery every 10 seconds.
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void recoverStaleWorkers() {

        LocalDateTime cutoff =
                LocalDateTime.now()
                        .minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);

        List<Worker> staleWorkers =
                workerRepository.findStaleWorkers(cutoff);

        for (Worker worker : staleWorkers) {

            recoverWorker(worker);
        }
    }


    private void recoverWorker(Worker worker) {

        log.warn("Stale worker detected: {} ({})", worker.getId(), worker.getName());

        /*
         * Lock worker before modifying it.
         */
        Worker lockedWorker =
                workerRepository
                        .findByIdForUpdate(worker.getId())
                        .orElse(null);

        if (lockedWorker == null) {
            return;
        }

        /*
         * Double check heartbeat.
         *
         * Worker might have sent a heartbeat
         * between detection and locking.
         */
        LocalDateTime cutoff =
                LocalDateTime.now()
                        .minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);

        if (lockedWorker.getLastHeartbeatAt() != null
                && lockedWorker.getLastHeartbeatAt().isAfter(cutoff)) {

            return;
        }

        /*
         * Mark worker OFFLINE.
         */
        lockedWorker.setStatus("OFFLINE");

        /*
         * Find all jobs currently running
         * on this worker.
         */
        List<Job> runningJobs =
                jobRepository.findRunningJobsByWorker(
                        lockedWorker.getId()
                );

        /*
         * Recover every running job.
         */
        for (Job job : runningJobs) {

            log.info("Recovering job {} from stale worker {}", job.getId(), lockedWorker.getId());

            job.setStatus("QUEUED");

            /*
             * Make the job immediately available
             * to another worker.
             */
            job.setRunAt(LocalDateTime.now());

            /*
             * Remove ownership of dead worker.
             */
            job.setClaimedBy(null);
            job.setClaimedAt(null);

            jobRepository.save(job);
        }

        /*
         * Worker no longer has active jobs.
         */
        lockedWorker.setActiveJobs(0);

        workerRepository.save(lockedWorker);

        log.info("Worker {} marked OFFLINE, {} job(s) recovered", lockedWorker.getId(), runningJobs.size());
    }
}