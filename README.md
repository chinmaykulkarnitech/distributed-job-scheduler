# Distributed Job Scheduler

A production-inspired distributed job scheduling platform designed to reliably execute asynchronous background jobs across multiple workers.

The system provides job queues, priority-based scheduling, delayed and recurring jobs, retries, worker management, heartbeats, execution tracking, logging, and Dead Letter Queue (DLQ) handling.

## Features

* JWT-based authentication
* Organization and project management
* Multiple queues per project
* Queue priority and concurrency configuration
* Immediate and delayed job execution
* Scheduled and recurring jobs
* Cron-based scheduling
* Atomic job claiming to prevent duplicate execution
* Concurrent worker execution
* Worker heartbeats and health monitoring
* Configurable retry policies
* Job execution history
* Execution logs
* Dead Letter Queue for permanently failed jobs
* Job status tracking
* REST APIs with Swagger/OpenAPI documentation
* Web dashboard for monitoring and management
* MySQL relational database
* Docker support

## Job Lifecycle

```text
                 ┌───────────┐
                 │   QUEUED  │
                 └─────┬─────┘
                       │
                       ▼
                ┌─────────────┐
                │   CLAIMED   │
                └──────┬──────┘
                       │
                       ▼
                ┌─────────────┐
                │   RUNNING   │
                └──────┬──────┘
                       │
              ┌────────┴────────┐
              ▼                 ▼
        ┌───────────┐      ┌──────────┐
        │ COMPLETED │      │  FAILED  │
        └───────────┘      └────┬─────┘
                                │
                         Retry available?
                          ┌─────┴─────┐
                         │            │
                        YES           NO
                         │            │
                         ▼            ▼
                     Retry Job       DEAD
                                      │
                                      ▼
                              Dead Letter Queue
```

## System Architecture

The platform follows a distributed worker architecture:

```text
┌──────────────────────┐
│      Frontend        │
│    Web Dashboard     │
└──────────┬───────────┘
           │ REST API
           ▼
┌──────────────────────┐
│     Spring Boot      │
│       Backend        │
│                      │
│ Authentication       │
│ Project Management   │
│ Queue Management     │
│ Job Scheduling       │
│ Job Management       │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│       MySQL          │
│      Database        │
└──────────┬───────────┘
           │
           │ Job Polling
           ▼
 ┌─────────────────────┐
 │      Workers        │
 │                     │
 │ Worker 1            │
 │ Worker 2            │
 │ Worker N            │
 └─────────┬───────────┘
           │
           ▼
      Job Execution
```

The complete architecture diagram is available in:

`docs/architecture-diagram.png`

## Database Design

The system uses a normalized MySQL relational schema containing the following major entities:

* Users
* Organizations
* Organization Members
* Projects
* Queues
* Jobs
* Job Executions
* Job Logs
* Retry Policies
* Workers
* Worker Heartbeats
* Scheduled Jobs
* Dead Letter Queue

Database schema:

`database/schema.sql`

ER diagram:

`docs/er-diagram.png`

The schema includes:

* Primary keys
* Foreign keys
* Unique constraints
* Composite indexes
* Check constraints
* Cascading behavior
* Job polling indexes
* Idempotency constraints
* Worker heartbeat indexes

## Job Reliability

The scheduler is designed around reliable job execution.

### Atomic Job Claiming

Workers must atomically claim a job before executing it. This prevents multiple workers from processing the same job concurrently.

### Idempotency

Jobs support an idempotency key scoped to a queue to help prevent accidental duplicate submissions.

### Retry Handling

Failed jobs can be retried according to a configured retry policy.

Supported retry strategies include:

* Fixed delay
* Linear backoff
* Exponential backoff

### Dead Letter Queue

When a job permanently fails after its retry attempts are exhausted, it can be moved to the Dead Letter Queue for inspection and recovery.

## Worker Management

Workers register with the scheduler and periodically send heartbeat information.

Worker information includes:

* Worker status
* Hostname
* Last heartbeat
* Concurrency limit
* Active jobs
* Startup time

Heartbeat information can be used to monitor worker health and detect unavailable workers.

## API Documentation

REST APIs are documented using Swagger/OpenAPI.

Start the backend and open:

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger provides interactive documentation for the available authentication, project, queue, job, worker, scheduling, execution, retry, and monitoring APIs.

A PDF export of the API documentation is available in:

docs/swagger-api.pdf

## Technology Stack

### Backend

* Java
* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* Hibernate
* Maven

### Frontend

* React
* Vite
* Material UI

### Database

* MySQL

### API Documentation

* Swagger
* OpenAPI

### Development Tools

* IntelliJ IDEA
* MySQL Workbench
* Postman
* Git
* GitHub
* Docker

## Project Structure

```text
distributed-job-scheduler/
│
├── backend/
│   ├── src/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   └── package.json
│
├── database/
│   └── schema.sql
│
├── docker/
│
├── docs/
|   ├── architecture-diagram.png
|   ├── er-diagram.png
|   ├── design-decisions.md
|   └── swagger-api.pdf
|
├── screenshots/
│   ├── swagger-api.png
│   ├── database-schema.png
│   ├── dashboard-overview.png
│   ├── job-execution.png
│   └── worker-monitoring.png
│
├── .gitignore
└── README.md
```

## Configuration

Sensitive configuration values are supplied through environment variables.

Example:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

Required environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
```

Sensitive credentials are not stored in the repository.

## Database Setup

Create the MySQL database:

```sql
CREATE DATABASE job_scheduler;
```

Then execute:

```text
database/schema.sql
```

The schema file creates the required database structure including tables, relationships, indexes, constraints, and foreign keys.

## Running the Backend

Configure the required environment variables and start the Spring Boot application.

Using Maven:

```bash
mvn spring-boot:run
```

The backend will normally start on:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## Running the Frontend

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

## Docker

Docker configuration is provided in the `docker/` directory.

Docker support is intended to simplify local deployment and environment setup.

## Testing

Critical functionality should be tested around:

* Authentication
* Job creation
* Queue management
* Job claiming
* Concurrent execution
* Retry handling
* Dead Letter Queue behavior
* Worker heartbeats
* Job execution history
* Idempotency

## Documentation

Project documentation and diagrams:

```text
docs/
├── architecture-diagram.png
└── er-diagram.png
```

API documentation is available through Swagger/OpenAPI.

## Screenshots

The `screenshots/` directory contains selected evidence of the implemented system:

* Swagger API documentation
* Database schema
* Dashboard
* Job execution
* Worker monitoring

## Engineering Considerations

The project focuses on the engineering aspects of distributed job scheduling rather than simply implementing CRUD operations.

Important design considerations include:

* Atomic job claiming
* Concurrent workers
* Idempotent job submission
* Retry strategies
* Worker health monitoring
* Execution history
* Database indexing
* Transactional consistency
* Failure handling
* Dead Letter Queue processing
* Maintainable service architecture

## Future Enhancements

Possible future improvements include:

* Workflow dependencies
* Distributed locking
* Queue sharding
* Rate limiting
* Event-driven execution
* WebSocket-based live updates
* Advanced role-based access control
* AI-generated failure summaries
* Horizontal worker autoscaling

## Internship Assignment

This project was developed as a production-inspired distributed systems implementation focusing on:

* Backend engineering
* Database design
* Concurrency
* Reliability
* REST API design
* Worker coordination
* Observability
* Full-stack development
* System architecture
* Maintainability
