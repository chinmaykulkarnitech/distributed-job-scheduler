# Design Decisions

## 1. Overview

The Distributed Job Scheduler is designed as a production-inspired platform for managing and executing asynchronous background jobs across multiple workers.

The design focuses on reliability, concurrency, database consistency, observability, and maintainability rather than implementing a large number of optional features.

The major design decisions are described below.

---

## 2. Technology Stack

### 2.1 Spring Boot

**Decision:** Use Spring Boot for the backend.

**Reason:**

Spring Boot provides a mature ecosystem for building REST APIs, database access, security, validation, dependency injection, scheduling, and application configuration.

It also provides good integration with:

* Spring Data JPA
* Spring Security
* MySQL
* JWT authentication
* OpenAPI/Swagger

This allows the application to remain modular while reducing boilerplate code.

**Trade-off:**

Spring Boot introduces framework complexity compared with a lightweight Java HTTP server, but the productivity, ecosystem, and maintainability benefits are more important for this project.

---

### 2.2 MySQL

**Decision:** Use MySQL as the primary relational database.

**Reason:**

The scheduler contains strongly related entities such as:

* Users
* Organizations
* Projects
* Queues
* Jobs
* Workers
* Job Executions
* Job Logs
* Scheduled Jobs
* Retry Policies
* Dead Letter Queue entries

A relational database provides:

* Referential integrity
* Transactions
* Foreign keys
* Unique constraints
* Indexes
* Consistent updates

These properties are particularly important when multiple workers interact with the same job records.

**Trade-off:**

A distributed NoSQL database could provide different scalability characteristics, but MySQL provides stronger relational consistency and is simpler and more appropriate for the current project scale.

---

## 3. Authentication

### 3.1 JWT Authentication

**Decision:** Use JWT-based authentication.

**Reason:**

The backend exposes REST APIs that need authenticated access. JWT provides a stateless authentication mechanism suitable for REST APIs.

The backend can validate a token on each request without maintaining a traditional server-side HTTP session.

The authentication flow is:

```text
User Login
    ↓
Credentials Validation
    ↓
JWT Token Generated
    ↓
Client Stores Token
    ↓
Token Sent With API Requests
    ↓
JWT Authentication Filter
    ↓
Authenticated Request
```

**Trade-off:**

JWT simplifies stateless authentication but requires careful handling of token expiration and secret management.

The JWT secret is therefore supplied through an environment variable rather than being stored directly in source code.

---

## 4. Job Claiming

### 4.1 Atomic Job Claiming

**Decision:** Jobs must be claimed atomically before execution.

**Reason:**

Multiple workers may poll the same queue simultaneously.

Without atomic claiming, the following race condition could occur:

```text
Worker A → finds Job 101
Worker B → finds Job 101
Worker A → executes Job 101
Worker B → executes Job 101
```

This would result in duplicate execution.

The scheduler therefore uses database state transitions and transactional operations to ensure that a job is claimed before a worker executes it.

Conceptually:

```text
QUEUED
   ↓
CLAIMED
   ↓
RUNNING
   ↓
COMPLETED
```

Only the worker that successfully claims the job should proceed with execution.

**Trade-off:**

Database-based coordination introduces database contention when the number of workers becomes very large. However, it provides a simple and reliable coordination mechanism for the current system.

---

## 5. Job Lifecycle

The job model uses explicit states to represent its lifecycle.

```text
QUEUED
   ↓
CLAIMED
   ↓
RUNNING
   ↓
COMPLETED
```

Failure can result in:

```text
RUNNING
   ↓
FAILED
   ↓
Retry Available?
   ├── YES → QUEUED
   └── NO  → DEAD
```

### Why explicit states?

Explicit job states make it possible to:

* Track job progress
* Recover interrupted jobs
* Monitor queues
* Identify failures
* Implement retries
* Move permanently failed jobs to the DLQ

This also makes the system easier to debug.

---

## 6. Worker Heartbeats

**Decision:** Workers periodically report their health using heartbeats.

Each heartbeat can contain information such as:

* Worker ID
* Heartbeat timestamp
* CPU usage
* Memory usage
* Active jobs

The worker table also maintains information such as:

* Worker status
* Hostname
* Last heartbeat
* Concurrency limit
* Active jobs

Conceptually:

```text
Worker
   │
   │ heartbeat
   ▼
Worker Heartbeats
   │
   ▼
Scheduler / Monitoring
```

### Why heartbeats?

A distributed scheduler needs a way to determine whether a worker is still available.

If a worker stops sending heartbeats, the scheduler can identify it as unhealthy and recovery mechanisms can be triggered.

**Trade-off:**

Heartbeats create additional database writes. However, the operational visibility and failure detection they provide are important for a distributed worker system.

---

## 7. Retry Policies

**Decision:** Retry behavior is configurable through retry policies.

Different jobs may have different failure characteristics. Retrying every job with the same delay is not appropriate.

The system therefore supports configurable retry behavior such as:

* Fixed delay
* Linear backoff
* Exponential backoff

Conceptually:

```text
Job Failure
     ↓
Retry Policy
     ↓
Calculate Delay
     ↓
Schedule Retry
     ↓
Execute Again
```

### Why backoff?

Backoff prevents a continuously failing job from immediately consuming worker and database resources.

Exponential backoff is particularly useful when failures may be caused by temporary external problems.

---

## 8. Dead Letter Queue

**Decision:** Permanently failed jobs are represented through a Dead Letter Queue.

A job should not retry indefinitely.

After the configured retry attempts are exhausted:

```text
Job
 ↓
FAILED
 ↓
Retry Limit Reached
 ↓
DEAD
 ↓
Dead Letter Queue
```

The DLQ stores information such as:

* Job ID
* Failure reason
* Failure timestamp
* Resolution status
* Resolution timestamp

### Why a DLQ?

The DLQ separates permanently failed jobs from normal queue processing.

This allows operators to:

* Investigate failures
* Inspect error messages
* Identify problematic jobs
* Resolve failures
* Retry or recover jobs when appropriate

It also prevents permanently failing jobs from continuously consuming worker capacity.

---

## 9. Database Indexes

**Decision:** Add indexes to frequently queried columns and query combinations.

The scheduler frequently performs operations such as:

* Finding jobs for a queue
* Finding jobs by status
* Finding jobs ready to run
* Finding jobs assigned to a worker
* Finding worker heartbeats
* Finding scheduled jobs
* Finding execution history

For example, jobs use a polling index covering:

```text
queue_id
status
priority
run_at
```

This supports common worker polling operations.

Other indexes include:

```text
jobs.claimed_by

jobs(status, run_at)

worker_heartbeats(worker_id, heartbeat_at)

job_executions(job_id)

job_executions(worker_id)

scheduled_jobs(enabled, next_run_at)
```

### Why indexes?

Without appropriate indexes, the database may need to scan large numbers of rows when workers poll for jobs.

Indexes reduce lookup cost and improve scheduler responsiveness as the number of jobs increases.

**Trade-off:**

Indexes improve read performance but require additional storage and can increase write/update cost.

Therefore, indexes are added to columns that are important for actual query patterns rather than indexing every column.

---

## 10. Idempotency Keys

**Decision:** Jobs support an idempotency key scoped to a queue.

The database uses a unique constraint:

```text
(queue_id, idempotency_key)
```

### Why?

Clients may accidentally submit the same job multiple times due to:

* Network retries
* Client retries
* Request timeouts
* Duplicate API requests

Without idempotency protection:

```text
Request 1 → Create Job A
Request 2 → Create Job B
```

could result in duplicate work.

With the idempotency key:

```text
queue_id + idempotency_key
            ↓
      Unique Constraint
            ↓
Duplicate submission prevented
```

**Trade-off:**

Idempotency keys require clients to generate and manage stable identifiers for operations where duplicate submission must be avoided.

---

## 11. Concurrency Handling

**Decision:** Workers execute jobs concurrently within a configured concurrency limit.

Each worker has a `concurrency_limit` and tracks its `active_jobs`.

Conceptually:

```text
Worker
Concurrency Limit = 3

      ┌─────────────┐
      │   Worker    │
      └──────┬──────┘
             │
      ┌──────┼──────┐
      ▼      ▼      ▼
    Job A  Job B   Job C
```

When the worker reaches its concurrency limit, it should not claim additional jobs until capacity becomes available.

### Why?

Concurrency improves throughput by allowing multiple independent jobs to execute simultaneously.

At the same time, limiting concurrency prevents a worker from consuming unlimited CPU, memory, or other resources.

**Trade-off:**

Higher concurrency can increase throughput but can also increase resource contention. Therefore, concurrency is configurable rather than unlimited.

---

## 12. Job Execution History

**Decision:** Maintain a separate `job_executions` table.

A job can execute multiple times because of retries.

Instead of overwriting the previous execution information, every attempt is recorded separately.

Example:

```text
Job 101

Execution 1 → FAILED
Execution 2 → FAILED
Execution 3 → COMPLETED
```

Each execution can contain:

* Job ID
* Worker ID
* Attempt number
* Status
* Start time
* Finish time
* Duration
* Error message

### Why?

This provides an execution history that can be used for:

* Debugging
* Monitoring
* Performance analysis
* Retry investigation
* Auditing

---

## 13. Job Logs

**Decision:** Store execution-related logs separately from the job record.

The `job_logs` table is associated with a specific job execution.

```text
Job
 ↓
Job Execution
 ↓
Job Logs
```

### Why?

Separating logs from the main job table avoids making the job record excessively large and allows multiple log entries for each execution.

It also makes execution history easier to inspect.

---

## 14. Scheduled Jobs

**Decision:** Store scheduling information separately from the main job record.

The `scheduled_jobs` table contains information such as:

* Schedule type
* Scheduled time
* Cron expression
* Next run time
* Last run time
* Timezone
* Enabled state

### Why?

Scheduling information has a different lifecycle from an individual job execution.

Separating the scheduling configuration from job data keeps the schema normalized and makes recurring scheduling easier to manage.

---

## 15. Foreign Keys and Cascading

**Decision:** Use foreign keys to maintain referential integrity.

Examples include:

```text
Project → Queue
Queue → Job
Worker → Worker Heartbeat
Job → Job Execution
Job Execution → Job Log
Job → Dead Letter Queue
```

Foreign keys prevent references to non-existent parent records.

Where appropriate, cascading behavior is used.

For example, deleting a queue can remove its dependent jobs where the lifecycle of those records is intentionally tied to the queue.

**Trade-off:**

Cascading simplifies cleanup but must be used carefully because deleting a parent can result in deletion of dependent records.

---

## 16. Database Constraints

The schema uses database-level constraints in addition to application-level validation.

Examples include:

* Primary keys
* Foreign keys
* Unique constraints
* Check constraints
* Non-null constraints

For example, job priority and attempt count are constrained to non-negative values.

Job status is also restricted to valid lifecycle states.

### Why?

Application validation alone is not sufficient when multiple components can interact with the database.

Database constraints provide a final layer of data integrity.

---

## 17. Separation of Responsibilities

The backend follows a layered structure:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

### Controllers

Handle HTTP requests and responses.

### Services

Contain business logic such as:

* Job creation
* Queue management
* Worker management
* Retry handling
* Recovery operations

### Repositories

Handle database access.

### Entities

Represent persistent database models.

### DTOs

Represent API request and response data.

### Why?

Separating responsibilities makes the system:

* Easier to test
* Easier to maintain
* Easier to extend
* Less tightly coupled

---

## 18. Environment-Based Configuration

**Decision:** Sensitive configuration is provided through environment variables.

Database credentials and JWT secrets are not hard-coded into the source code.

The application uses properties such as:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

### Why?

This prevents credentials from being accidentally committed to GitHub and allows different environments to use different configurations.

For example:

```text
Development
    ↓
Local MySQL

Testing
    ↓
Test Database

Production
    ↓
Production Database
```

The application code does not need to change between environments.

---

## 19. Swagger / OpenAPI

**Decision:** Use Swagger/OpenAPI for API documentation.

The REST API is documented through Swagger UI.

This provides:

* Endpoint discovery
* Request/response documentation
* Authentication testing
* Interactive API testing

It also makes the project easier for reviewers and developers to understand.

---

## 20. Overall Design Trade-off

The system prioritizes **correctness, reliability, maintainability, and clear database coordination** over premature distributed infrastructure complexity.

For the current project, database-backed coordination provides a practical foundation for:

* Atomic job claiming
* Job state management
* Worker monitoring
* Retry handling
* Execution tracking
* Idempotency

For a much larger production deployment, additional technologies could be introduced, such as distributed locks, message brokers, queue sharding, event-driven execution, or horizontal worker autoscaling.

These were intentionally treated as future enhancements rather than adding unnecessary complexity to the current implementation.

---

## 21. Summary

The major architectural decisions are centered around reliable asynchronous job execution.

| Requirement           | Design Decision                 |
| --------------------- | ------------------------------- |
| Backend               | Spring Boot                     |
| Database              | MySQL                           |
| Authentication        | JWT                             |
| Job coordination      | Database-backed atomic claiming |
| Worker monitoring     | Heartbeats                      |
| Failure handling      | Retry policies                  |
| Permanent failures    | Dead Letter Queue               |
| Duplicate submissions | Idempotency keys                |
| Query performance     | Targeted indexes                |
| Execution history     | Job executions                  |
| Logging               | Job logs                        |
| Scheduling            | Scheduled jobs                  |
| API documentation     | Swagger/OpenAPI                 |
| Configuration         | Environment variables           |
| Maintainability       | Layered backend architecture    |

These decisions provide a foundation for a reliable and maintainable distributed job scheduling platform while leaving room for future scaling and advanced distributed-system features.
