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

## 🐳 Docker Setup

The application can be run using Docker Compose, which starts the MySQL database, Spring Boot backend, and React frontend as separate containers.

### Prerequisites

Install:

* Docker Desktop
* Git

Verify the installation:

```bash
docker --version
docker compose version
```

### Docker Files

```text
docker/
├── Dockerfile.backend
├── Dockerfile.frontend
└── docker-compose.yml
```

### Environment Variables

Create a `.env` file in the **project root**:

```text
distributed-job-scheduler/
├── .env
├── backend/
├── frontend/
├── database/
├── docker/
├── docs/
└── README.md
```

Example:

```env
DB_PASSWORD=change_me
JWT_SECRET=change_this_to_a_secure_secret
JWT_EXPIRATION=86400000
```

> `.env` contains local secrets and must not be committed to GitHub.

### Start the Application with Docker

From the project root:

```bash
docker compose --env-file .env -f docker/docker-compose.yml up --build
```

This command:

1. Builds the Spring Boot backend image.
2. Builds the React frontend image.
3. Pulls the MySQL image if required.
4. Creates the Docker network.
5. Creates the MySQL database using `database/schema.sql`.
6. Starts all application containers.

### Services

After startup:

| Service     | Address                                     |
| ----------- | ------------------------------------------- |
| Frontend    | http://localhost:3000                       |
| Backend API | http://localhost:8081                       |
| Swagger UI  | http://localhost:8081/swagger-ui/index.html |
| MySQL       | localhost:3307                              |

### Docker Port Mapping

```text
Frontend:
localhost:3000 → frontend container port 80

Backend:
localhost:8081 → backend container port 8080

MySQL:
localhost:3307 → MySQL container port 3306

Swagger   → http://localhost:8081/swagger-ui/index.html

```

The backend connects to MySQL **inside the Docker network** using:

```text
jdbc:mysql://mysql:3306/job_scheduler
```

The backend should therefore **not** use `localhost:3307` for its Docker database connection.

### Check Container Status

```bash
docker compose --env-file .env -f docker/docker-compose.yml ps
```

Expected containers:

```text
job-scheduler-mysql
job-scheduler-backend
job-scheduler-frontend
```

### View Logs

View all logs:

```bash
docker compose --env-file .env -f docker/docker-compose.yml logs
```

Backend logs:

```bash
docker compose --env-file .env -f docker/docker-compose.yml logs backend
```

Frontend logs:

```bash
docker compose --env-file .env -f docker/docker-compose.yml logs frontend
```

MySQL logs:

```bash
docker compose --env-file .env -f docker/docker-compose.yml logs mysql
```

### Stop the Application

Stop the containers:

```bash
docker compose --env-file .env -f docker/docker-compose.yml down
```

This stops and removes the containers while preserving the named MySQL volume.

### Start Again Without Rebuilding

```bash
docker compose --env-file .env -f docker/docker-compose.yml up
```

### Rebuild After Source Code Changes

After changing the backend or frontend source:

```bash
docker compose --env-file .env -f docker/docker-compose.yml up --build
```

The Docker images are rebuilt using the **current contents** of:

```text
backend/
frontend/
```

### Database Initialization

The Compose configuration mounts:

```text
database/schema.sql
```

into the MySQL initialization directory.

For a **new MySQL volume**, the schema is executed automatically when the MySQL container initializes.

### Reset the Docker Database

To completely remove the Docker database and recreate it from `schema.sql`:

```bash
docker compose --env-file .env -f docker/docker-compose.yml down -v
```

Then:

```bash
docker compose --env-file .env -f docker/docker-compose.yml up --build
```

> `down -v` removes the MySQL Docker volume and permanently deletes the data stored in that Docker database.

### Docker API Documentation

After the backend container is running, Swagger UI is available at:

```text
http://localhost:8081/swagger-ui/index.html
```

### Docker Project Flow

```text
                    Docker Compose
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
     React Frontend  Spring Boot      MySQL
      Port 3000       Port 8081      Port 3307
          │              │              │
          └──────────────┤              │
                         └──────► mysql:3306
```


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
