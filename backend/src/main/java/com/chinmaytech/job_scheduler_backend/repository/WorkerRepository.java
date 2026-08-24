package com.chinmaytech.job_scheduler_backend.repository;

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
public interface WorkerRepository extends JpaRepository<Worker, String> {

    List<Worker> findByStatus(String status);
    Optional<Worker> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Worker w WHERE w.id = :id")
    Optional<Worker> findByIdForUpdate(@Param("id") String id);


    @Query("""
    SELECT w
    FROM Worker w
    WHERE w.status = 'ONLINE'
      AND w.lastHeartbeatAt < :cutoff
""")
    List<Worker> findStaleWorkers(
            @Param("cutoff") LocalDateTime cutoff
    );


}