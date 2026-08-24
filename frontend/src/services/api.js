import axios from 'axios';

// Relative base URL: in dev, Vite's proxy (see vite.config.js) forwards
// /api/* to the Spring Boot backend so there's no CORS issue. In a
// production build, serve the frontend behind the same host/reverse proxy
// as the backend, or set VITE_API_BASE_URL at build time.
const baseURL = import.meta.env.VITE_API_BASE_URL_RUNTIME || '';

export const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const TOKEN_KEY = 'job_scheduler_token';
const USER_KEY = 'job_scheduler_user';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setAuth(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// Attach the JWT to every request automatically.
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Normalize backend errors into a plain message string, and force a
// logout + redirect on 401/403 (expired or invalid token).
let onUnauthorized = null;
export function registerUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

export function extractErrorMessage(error) {
  if (error.response?.data) {
    const data = error.response.data;
    if (data.message) {
      // Field-level validation errors (400 from MethodArgumentNotValidException)
      if (data.errors && typeof data.errors === 'object') {
        const details = Object.values(data.errors).join(', ');
        return details ? `${data.message}: ${details}` : data.message;
      }
      return data.message;
    }
    if (typeof data === 'string') return data;
  }
  if (error.message === 'Network Error') {
    return 'Could not reach the server. Is the backend running?';
  }
  return 'An unexpected error occurred';
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      // Don't force-logout on a 403 that's just "not your resource" for
      // an otherwise-valid session vs. an actually expired token. The
      // backend only returns 403 for AccessDeniedException, and 401 for
      // bad/missing JWT — either way, on this app's protected pages, we
      // treat both as "auth is no longer valid".
      clearAuth();
      if (onUnauthorized) onUnauthorized();
    }
    error.friendlyMessage = extractErrorMessage(error);
    return Promise.reject(error);
  }
);

// ---------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------
export const AuthAPI = {
  register: (payload) => api.post('/api/auth/register', payload),
  login: (payload) => api.post('/api/auth/login', payload),
};

// ---------------------------------------------------------------------
// Organizations
// ---------------------------------------------------------------------
export const OrganizationAPI = {
  create: (payload) => api.post('/api/organizations', payload),
  getMine: () => api.get('/api/organizations'),
};

// ---------------------------------------------------------------------
// Projects
// ---------------------------------------------------------------------
export const ProjectAPI = {
  create: (payload) => api.post('/api/projects', payload),
  getByOrganization: (organizationId) =>
    api.get(`/api/projects/organization/${organizationId}`),
};

// ---------------------------------------------------------------------
// Queues
// ---------------------------------------------------------------------
export const QueueAPI = {
  create: (payload) => api.post('/api/queues', payload),
  getByProject: (projectId) => api.get(`/api/queues/project/${projectId}`),
  getOne: (queueId) => api.get(`/api/queues/${queueId}`),
  pause: (queueId) => api.put(`/api/queues/${queueId}/pause`),
  resume: (queueId) => api.put(`/api/queues/${queueId}/resume`),
  assignRetryPolicy: (queueId, retryPolicyId) =>
    api.put(`/api/queues/${queueId}/retry-policy/${retryPolicyId}`),
};

// ---------------------------------------------------------------------
// Jobs
// ---------------------------------------------------------------------
export const JobAPI = {
  create: (payload) => api.post('/api/jobs', payload),
  getAll: () => api.get('/api/jobs'),
  getOne: (jobId) => api.get(`/api/jobs/${jobId}`),
  getByQueue: (queueId) => api.get(`/api/jobs/queue/${queueId}`),
  createBatch: (jobs) => api.post('/api/jobs/batch', { jobs }),
};

// ---------------------------------------------------------------------
// Workers
// ---------------------------------------------------------------------
export const WorkerAPI = {
  register: (name) =>
    api.post(`/api/workers/register`, null, { params: { name } }),
  getOne: (workerId) => api.get(`/api/workers/${workerId}`),
  getOnline: () => api.get('/api/workers/online'),
  heartbeat: (workerId, { cpuUsage, memoryUsage, activeJobs } = {}) =>
    api.post(`/api/workers/${workerId}/heartbeat`, null, {
      params: { cpuUsage, memoryUsage, activeJobs },
    }),
  claimJob: (workerId) => api.post(`/api/workers/${workerId}/claim`),
  completeJob: (workerId, jobId) =>
    api.post(`/api/workers/${workerId}/jobs/${jobId}/complete`),
  failJob: (workerId, jobId) =>
    api.post(`/api/workers/${workerId}/jobs/${jobId}/fail`),
};

// ---------------------------------------------------------------------
// Retry policies
// ---------------------------------------------------------------------
export const RetryPolicyAPI = {
  create: (payload) => api.post('/api/retry-policies', payload),
  getAll: () => api.get('/api/retry-policies'),
  getOne: (id) => api.get(`/api/retry-policies/${id}`),
  update: (id, payload) => api.put(`/api/retry-policies/${id}`, payload),
  remove: (id) => api.delete(`/api/retry-policies/${id}`),
};
