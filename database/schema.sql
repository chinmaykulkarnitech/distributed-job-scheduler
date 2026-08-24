-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: job_scheduler
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `dead_letter_queue`
--

DROP TABLE IF EXISTS `dead_letter_queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dead_letter_queue` (
  `id` char(36) NOT NULL,
  `job_id` char(36) NOT NULL,
  `reason` text NOT NULL,
  `failed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved` tinyint(1) NOT NULL DEFAULT '0',
  `resolved_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dlq_job` (`job_id`),
  KEY `idx_dlq_resolved` (`resolved`,`failed_at`),
  CONSTRAINT `fk_dlq_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_executions`
--

DROP TABLE IF EXISTS `job_executions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_executions` (
  `id` char(36) NOT NULL,
  `job_id` char(36) NOT NULL,
  `worker_id` char(36) DEFAULT NULL,
  `attempt_number` int NOT NULL,
  `status` varchar(30) NOT NULL,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `finished_at` timestamp NULL DEFAULT NULL,
  `duration_ms` bigint DEFAULT NULL,
  `error_message` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_executions_job` (`job_id`),
  KEY `idx_job_executions_worker` (`worker_id`),
  CONSTRAINT `fk_execution_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_execution_worker` FOREIGN KEY (`worker_id`) REFERENCES `workers` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_execution_attempt` CHECK ((`attempt_number` > 0)),
  CONSTRAINT `chk_execution_status` CHECK ((`status` in (_utf8mb4'RUNNING',_utf8mb4'COMPLETED',_utf8mb4'FAILED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `job_logs`
--

DROP TABLE IF EXISTS `job_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_logs` (
  `id` char(36) NOT NULL,
  `job_execution_id` char(36) NOT NULL,
  `level` varchar(20) NOT NULL,
  `message` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_log_execution` (`job_execution_id`),
  CONSTRAINT `fk_log_execution` FOREIGN KEY (`job_execution_id`) REFERENCES `job_executions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_log_level` CHECK ((`level` in (_utf8mb4'INFO',_utf8mb4'WARN',_utf8mb4'ERROR',_utf8mb4'DEBUG')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `jobs`
--

DROP TABLE IF EXISTS `jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jobs` (
  `id` char(36) NOT NULL,
  `queue_id` char(36) NOT NULL,
  `job_type` varchar(100) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'QUEUED',
  `priority` int NOT NULL DEFAULT '0',
  `payload` json NOT NULL,
  `run_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `attempt_count` int NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `idempotency_key` varchar(255) DEFAULT NULL,
  `claimed_by` char(36) DEFAULT NULL,
  `claimed_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_jobs_queue_idempotent` (`queue_id`,`idempotency_key`),
  KEY `idx_jobs_polling` (`queue_id`,`status`,`priority`,`run_at`),
  KEY `idx_jobs_claimed_by` (`claimed_by`),
  KEY `idx_jobs_run_at` (`status`,`run_at`),
  CONSTRAINT `fk_job_claimed_worker` FOREIGN KEY (`claimed_by`) REFERENCES `workers` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_job_queue` FOREIGN KEY (`queue_id`) REFERENCES `queues` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_job_attempt_count` CHECK ((`attempt_count` >= 0)),
  CONSTRAINT `chk_job_priority` CHECK ((`priority` >= 0)),
  CONSTRAINT `chk_job_status` CHECK ((`status` in (_utf8mb4'QUEUED',_utf8mb4'SCHEDULED',_utf8mb4'CLAIMED',_utf8mb4'RUNNING',_utf8mb4'COMPLETED',_utf8mb4'FAILED',_utf8mb4'DEAD')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization_members`
--

DROP TABLE IF EXISTS `organization_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organization_members` (
  `id` char(36) NOT NULL,
  `organization_id` char(36) NOT NULL,
  `user_id` char(36) NOT NULL,
  `role` varchar(30) NOT NULL,
  `joined_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_user` (`organization_id`,`user_id`),
  KEY `idx_org_members_user` (`user_id`),
  KEY `idx_org_members_org` (`organization_id`),
  CONSTRAINT `fk_org_member_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_org_member_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_org_member_role` CHECK ((`role` in (_utf8mb4'OWNER',_utf8mb4'ADMIN',_utf8mb4'MEMBER',_utf8mb4'VIEWER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organizations`
--

DROP TABLE IF EXISTS `organizations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organizations` (
  `id` char(36) NOT NULL,
  `name` varchar(150) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `id` char(36) NOT NULL,
  `organization_id` char(36) NOT NULL,
  `name` varchar(150) NOT NULL,
  `description` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_name` (`organization_id`,`name`),
  CONSTRAINT `fk_project_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `queues`
--

DROP TABLE IF EXISTS `queues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `queues` (
  `id` char(36) NOT NULL,
  `project_id` char(36) NOT NULL,
  `retry_policy_id` char(36) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `priority` int NOT NULL DEFAULT '0',
  `concurrency_limit` int NOT NULL DEFAULT '1',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_queue_name` (`project_id`,`name`),
  KEY `fk_queue_retry_policy` (`retry_policy_id`),
  CONSTRAINT `fk_queue_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_queue_retry_policy` FOREIGN KEY (`retry_policy_id`) REFERENCES `retry_policies` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_queue_concurrency` CHECK ((`concurrency_limit` > 0)),
  CONSTRAINT `chk_queue_priority` CHECK ((`priority` >= 0)),
  CONSTRAINT `chk_queue_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'PAUSED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `retry_policies`
--

DROP TABLE IF EXISTS `retry_policies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `retry_policies` (
  `id` char(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `strategy` varchar(30) NOT NULL,
  `max_attempts` int NOT NULL,
  `initial_delay_seconds` int NOT NULL,
  `max_delay_seconds` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_initial_delay` CHECK ((`initial_delay_seconds` >= 0)),
  CONSTRAINT `chk_max_attempts` CHECK ((`max_attempts` > 0)),
  CONSTRAINT `chk_max_delay` CHECK ((`max_delay_seconds` >= 0)),
  CONSTRAINT `chk_retry_delay_range` CHECK ((`max_delay_seconds` >= `initial_delay_seconds`)),
  CONSTRAINT `chk_retry_strategy` CHECK ((`strategy` in (_utf8mb4'FIXED',_utf8mb4'LINEAR',_utf8mb4'EXPONENTIAL')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scheduled_jobs`
--

DROP TABLE IF EXISTS `scheduled_jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scheduled_jobs` (
  `id` char(36) NOT NULL,
  `job_id` char(36) DEFAULT NULL,
  `schedule_type` varchar(20) NOT NULL,
  `scheduled_at` timestamp NULL DEFAULT NULL,
  `cron_expression` varchar(100) DEFAULT NULL,
  `next_run_at` timestamp NULL DEFAULT NULL,
  `last_run_at` timestamp NULL DEFAULT NULL,
  `timezone` varchar(50) DEFAULT 'UTC',
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_scheduled_job` (`job_id`),
  KEY `idx_scheduled_jobs_next_run` (`enabled`,`next_run_at`),
  CONSTRAINT `fk_scheduled_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_schedule_type` CHECK ((`schedule_type` in (_utf8mb4'ONCE',_utf8mb4'CRON')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` char(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `worker_heartbeats`
--

DROP TABLE IF EXISTS `worker_heartbeats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `worker_heartbeats` (
  `id` char(36) NOT NULL,
  `worker_id` char(36) NOT NULL,
  `heartbeat_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cpu_usage` decimal(5,2) DEFAULT NULL,
  `memory_usage` decimal(5,2) DEFAULT NULL,
  `active_jobs` int NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_worker_heartbeats_worker_time` (`worker_id`,`heartbeat_at`),
  CONSTRAINT `fk_heartbeat_worker` FOREIGN KEY (`worker_id`) REFERENCES `workers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `workers`
--

DROP TABLE IF EXISTS `workers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `workers` (
  `id` char(36) NOT NULL,
  `name` varchar(100) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ONLINE',
  `hostname` varchar(255) DEFAULT NULL,
  `last_heartbeat_at` timestamp NULL DEFAULT NULL,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `concurrency_limit` int NOT NULL DEFAULT '1',
  `active_jobs` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_workers_status_heartbeat` (`status`,`last_heartbeat_at`),
  CONSTRAINT `chk_worker_active_jobs` CHECK ((`active_jobs` >= 0)),
  CONSTRAINT `chk_worker_status` CHECK ((`status` in (_utf8mb4'ONLINE',_utf8mb4'OFFLINE',_utf8mb4'DRAINING')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-25  2:10:35
