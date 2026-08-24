package com.chinmaytech.job_scheduler_backend.repository;


import com.chinmaytech.job_scheduler_backend.entity.Job;
import com.chinmaytech.job_scheduler_backend.entity.Worker;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findByQueueId(String queueId);

    List<Job> findByQueueIdAndStatus(
            String queueId,
            String status
    );

    boolean existsByQueueIdAndIdempotencyKey(
            String queueId,
            String idempotencyKey
    );

    Optional<Job> findByQueueIdAndIdempotencyKey(
            String queueId,
            String idempotencyKey
    );

    List<Job> findByClaimedByAndStatus(
            String workerId,
            String status
    );



    List<Job> findAllByOrderByCreatedAtDesc();

    // FIND ONE AVAILABLE JOB FOR A WORKER
    @Query(value = """
        SELECT j.*
        FROM jobs j
        JOIN queues q ON j.queue_id = q.id
        WHERE j.status = 'QUEUED'
          AND j.run_at <= :now
          AND q.status = 'ACTIVE'
        ORDER BY j.priority DESC, j.run_at ASC, j.created_at ASC
        LIMIT 1
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    Optional<Job> findNextAvailableJob(
            @Param("now") LocalDateTime now
    );
    @Query("""
    SELECT j
    FROM Job j
    WHERE j.status = 'RUNNING'
      AND j.claimedAt < :cutoff
""")
    List<Job> findStaleRunningJobs(
            @Param("cutoff") LocalDateTime cutoff
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.id = :id")
    Optional<Job> findByIdForUpdate(@Param("id") String id);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT j
        FROM Job j
        WHERE j.claimedBy = :workerId
          AND j.status = 'RUNNING'
    """)
    List<Job> findRunningJobsByWorker(
            @Param("workerId") String workerId
    );
}
