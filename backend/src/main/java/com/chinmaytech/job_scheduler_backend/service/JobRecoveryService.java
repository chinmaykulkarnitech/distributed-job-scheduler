package com.chinmaytech.job_scheduler_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//import com.chinmaytech.job_scheduler_backend.entity.Job;
//import com.chinmaytech.job_scheduler_backend.repository.JobRepository;
//import com.chinmaytech.job_scheduler_backend.repository.WorkerRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
@Service
@RequiredArgsConstructor
public class JobRecoveryService {
//
//    private final JobRepository jobRepository;
//    private final WorkerRepository workerRepository;
//
//    @Transactional
//    public void recoverStaleJobs() {
//
//        LocalDateTime cutoff =
//               LocalDateTime.now().minusMinutes(5);
//
////       LocalDateTime cutoff =
////                LocalDateTime.now().minusSeconds(30);
//
//        List<Job> staleJobs =
//                jobRepository.findStaleRunningJobs(cutoff);
//
//        for (Job job : staleJobs) {
//
//            String workerId = job.getClaimedBy();
//
//            if (workerId != null) {
//
//                workerRepository.findByIdForUpdate(workerId)
//                        .ifPresent(worker -> {
//
//                            if (worker.getActiveJobs() > 0) {
//                                worker.setActiveJobs(
//                                        worker.getActiveJobs() - 1
//                                );
//                                workerRepository.save(worker);
//                            }
//                        });
//            }
//
//            job.setStatus("QUEUED");
//            job.setClaimedBy(null);
//            job.setClaimedAt(null);
//
//            jobRepository.save(job);
//        }
//    }
//}\

}