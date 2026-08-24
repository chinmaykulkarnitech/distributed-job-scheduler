package com.chinmaytech.job_scheduler_backend.repository;

import com.chinmaytech.job_scheduler_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
