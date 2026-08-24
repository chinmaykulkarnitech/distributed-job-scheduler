package com.chinmaytech.job_scheduler_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobSchedulerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobSchedulerBackendApplication.class, args);
	}

}
