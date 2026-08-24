import { useEffect, useState, useCallback } from 'react';
import { Stack, TextField, MenuItem, CircularProgress } from '@mui/material';
import { OrganizationAPI, ProjectAPI, QueueAPI } from '../services/api';

/**
 * Cascading picker: Organization -> Project -> Queue.
 * The backend has no "list all queues" endpoint, only queues-by-project and
 * projects-by-organization, so selecting a queue means walking down through
 * an organization and a project first.
 *
 * Controlled via value={{ organizationId, projectId, queueId }} and
 * onChange(nextValue). Set `upTo="project"` to stop at the project level
 * (used on the Queues page, which only needs a project).
 */
export default function OrgProjectQueuePicker({ value, onChange, upTo = 'queue', size = 'small' }) {
  const [organizations, setOrganizations] = useState([]);
  const [projects, setProjects] = useState([]);
  const [queues, setQueues] = useState([]);
  const [loadingOrgs, setLoadingOrgs] = useState(true);
  const [loadingProjects, setLoadingProjects] = useState(false);
  const [loadingQueues, setLoadingQueues] = useState(false);

  const { organizationId = '', projectId = '', queueId = '' } = value || {};

  const loadOrganizations = useCallback(async () => {
    setLoadingOrgs(true);
    try {
      const { data } = await OrganizationAPI.getMine();
      setOrganizations(data || []);
    } finally {
      setLoadingOrgs(false);
    }
  }, []);

  useEffect(() => {
    loadOrganizations();
  }, [loadOrganizations]);

  useEffect(() => {
    if (!organizationId) {
      setProjects([]);
      return;
    }
    setLoadingProjects(true);
    ProjectAPI.getByOrganization(organizationId)
      .then(({ data }) => setProjects(data || []))
      .finally(() => setLoadingProjects(false));
  }, [organizationId]);

  useEffect(() => {
    if (upTo !== 'queue' || !projectId) {
      setQueues([]);
      return;
    }
    setLoadingQueues(true);
    QueueAPI.getByProject(projectId)
      .then(({ data }) => setQueues(data || []))
      .finally(() => setLoadingQueues(false));
  }, [projectId, upTo]);

  const emit = (patch) => onChange({ organizationId, projectId, queueId, ...patch });

  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
      <TextField
        select
        label="Organization"
        size={size}
        value={organizationId}
        onChange={(e) => emit({ organizationId: e.target.value, projectId: '', queueId: '' })}
        sx={{ minWidth: 200 }}
        SelectProps={{
          IconComponent: loadingOrgs ? () => <CircularProgress size={16} sx={{ mr: 1 }} /> : undefined,
        }}
      >
        {organizations.length === 0 && !loadingOrgs && (
          <MenuItem value="" disabled>
            No organizations yet
          </MenuItem>
        )}
        {organizations.map((org) => (
          <MenuItem key={org.id} value={org.id}>
            {org.name}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        label="Project"
        size={size}
        value={projectId}
        onChange={(e) => emit({ projectId: e.target.value, queueId: '' })}
        disabled={!organizationId}
        sx={{ minWidth: 200 }}
      >
        {projects.length === 0 && !loadingProjects && (
          <MenuItem value="" disabled>
            {organizationId ? 'No projects yet' : 'Select organization first'}
          </MenuItem>
        )}
        {projects.map((p) => (
          <MenuItem key={p.id} value={p.id}>
            {p.name}
          </MenuItem>
        ))}
      </TextField>

      {upTo === 'queue' && (
        <TextField
          select
          label="Queue"
          size={size}
          value={queueId}
          onChange={(e) => emit({ queueId: e.target.value })}
          disabled={!projectId}
          sx={{ minWidth: 200 }}
        >
          {queues.length === 0 && !loadingQueues && (
            <MenuItem value="" disabled>
              {projectId ? 'No queues yet' : 'Select project first'}
            </MenuItem>
          )}
          {queues.map((q) => (
            <MenuItem key={q.id} value={q.id}>
              {q.name}
            </MenuItem>
          ))}
        </TextField>
      )}
    </Stack>
  );
}
