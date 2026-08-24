# Job Scheduler — Frontend

React + Vite + Material UI dashboard for the existing Spring Boot
Distributed Job Scheduler backend. This project only *consumes* the
backend's REST APIs exactly as they exist — nothing in the backend was
changed.

## Run it

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173`. Make sure the Spring Boot backend
is running at `http://localhost:8080` (default `application.properties`).

Dev requests to `/api/*` are proxied to the backend by Vite (see
`vite.config.js`), so the browser never talks to `localhost:8080` directly
and no CORS configuration is required on the backend. If your backend runs
on a different host/port, set `VITE_API_BASE_URL` in `.env`.

## What's implemented

- **Auth** — register, login, JWT stored in `localStorage`, attached to
  every request via an Axios interceptor. A 401/403 response clears the
  session and redirects to `/login`.
- **Dashboard** — job counts by status, online worker count, a status
  distribution bar, recent jobs, and worker status — all computed from
  `GET /api/jobs` and `GET /api/workers/online` (the backend has no
  dedicated stats endpoint).
- **Projects** — pick/create an organization, then list/create projects
  in it (`/api/organizations`, `/api/projects`).
- **Queues** — pick an organization + project, list/create queues, pause
  and resume them, optionally assign a retry policy
  (`/api/queues`, `/api/retry-policies`).
- **Jobs** — table of all jobs the user can see (`GET /api/jobs`), create
  job dialog matching `CreateJobRequest` exactly, job details page, and
  Complete/Fail actions for RUNNING jobs (using the job's `claimedBy`
  worker, matching how `WorkerController` expects those calls).
- **Workers** — online worker list (`GET /api/workers/online`), register
  a worker, send a heartbeat, and look up any worker by ID (including
  offline ones) via `GET /api/workers/{id}`.

### A note on "claiming" a job

The backend's claim endpoint (`POST /api/workers/{workerId}/claim`) claims
whichever job is *next available* for that worker — it doesn't accept a
specific job ID. So instead of a per-row "Claim" button, the Jobs page has
a **"Claim Next Job"** control where you pick an online worker (the
"acting worker") and claim on its behalf. This matches the real API
instead of pretending a "claim this exact job" endpoint exists.

### Things intentionally left out

The backend doesn't expose a "list all workers" endpoint (only
`/online` and `/{id}`), so there's no way to show an "Offline Workers"
count on the dashboard, or a full worker directory — the UI only shows
what the API can answer. No new backend endpoints were added to work
around this.

## Structure

```
src/
├── components/     Sidebar, Navbar, ProtectedRoute, Loading, ErrorAlert, ...
├── pages/          Login, Register, Dashboard, Projects, Queues, Jobs, JobDetails, Workers
├── services/api.js Axios instance + JWT interceptor + one function per backend endpoint
├── context/         AuthContext.jsx
├── theme/           MUI theme
├── App.jsx
└── main.jsx
```
