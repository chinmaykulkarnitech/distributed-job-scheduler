import { useEffect, useState, useCallback } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  TextField,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stack,
  IconButton,
  Tooltip,
} from '@mui/material';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined';
import { OrganizationAPI, ProjectAPI } from '../services/api';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';

export default function Projects() {
  const [organizations, setOrganizations] = useState([]);
  const [organizationId, setOrganizationId] = useState('');
  const [projects, setProjects] = useState([]);

  const [loadingOrgs, setLoadingOrgs] = useState(true);
  const [loadingProjects, setLoadingProjects] = useState(false);
  const [error, setError] = useState('');

  const [orgDialogOpen, setOrgDialogOpen] = useState(false);
  const [orgName, setOrgName] = useState('');
  const [orgSubmitting, setOrgSubmitting] = useState(false);
  const [orgError, setOrgError] = useState('');

  const [projectDialogOpen, setProjectDialogOpen] = useState(false);
  const [projectForm, setProjectForm] = useState({ name: '', description: '' });
  const [projectSubmitting, setProjectSubmitting] = useState(false);
  const [projectError, setProjectError] = useState('');

  const loadOrganizations = useCallback(async () => {
    setLoadingOrgs(true);
    setError('');
    try {
      const { data } = await OrganizationAPI.getMine();
      setOrganizations(data || []);
      if (data?.length && !organizationId) {
        setOrganizationId(data[0].id);
      }
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load organizations');
    } finally {
      setLoadingOrgs(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadProjects = useCallback(async (orgId) => {
    if (!orgId) {
      setProjects([]);
      return;
    }
    setLoadingProjects(true);
    setError('');
    try {
      const { data } = await ProjectAPI.getByOrganization(orgId);
      setProjects(data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load projects');
    } finally {
      setLoadingProjects(false);
    }
  }, []);

  useEffect(() => {
    loadOrganizations();
  }, [loadOrganizations]);

  useEffect(() => {
    loadProjects(organizationId);
  }, [organizationId, loadProjects]);

  const handleCreateOrg = async (e) => {
    e.preventDefault();
    setOrgError('');
    setOrgSubmitting(true);
    try {
      const { data } = await OrganizationAPI.create({ name: orgName });
      setOrganizations((prev) => [...prev, data]);
      setOrganizationId(data.id);
      setOrgName('');
      setOrgDialogOpen(false);
    } catch (err) {
      setOrgError(err.friendlyMessage || 'Failed to create organization');
    } finally {
      setOrgSubmitting(false);
    }
  };

  const handleCreateProject = async (e) => {
    e.preventDefault();
    setProjectError('');
    setProjectSubmitting(true);
    try {
      const { data } = await ProjectAPI.create({
        organizationId,
        name: projectForm.name,
        description: projectForm.description,
      });
      setProjects((prev) => [data, ...prev]);
      setProjectForm({ name: '', description: '' });
      setProjectDialogOpen(false);
    } catch (err) {
      setProjectError(err.friendlyMessage || 'Failed to create project');
    } finally {
      setProjectSubmitting(false);
    }
  };

  if (loadingOrgs) return <Loading label="Loading organizations..." />;

  return (
    <Box>
      <ErrorAlert message={error} onClose={() => setError('')} />

      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }} justifyContent="space-between">
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems={{ sm: 'center' }} flexGrow={1}>
            <TextField
              select
              label="Organization"
              size="small"
              value={organizationId}
              onChange={(e) => setOrganizationId(e.target.value)}
              sx={{ minWidth: 240 }}
            >
              {organizations.length === 0 && (
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
            <Button variant="text" startIcon={<AddOutlinedIcon />} onClick={() => setOrgDialogOpen(true)}>
              New organization
            </Button>
          </Stack>
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh">
              <IconButton size="small" onClick={() => loadProjects(organizationId)}>
                <RefreshOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button
              variant="contained"
              startIcon={<AddOutlinedIcon />}
              disabled={!organizationId}
              onClick={() => setProjectDialogOpen(true)}
            >
              Create Project
            </Button>
          </Stack>
        </Stack>
      </Paper>

      {loadingProjects ? (
        <Loading label="Loading projects..." />
      ) : (
        <Grid container spacing={2}>
          {projects.map((project) => (
            <Grid item xs={12} sm={6} md={4} key={project.id}>
              <Card variant="outlined" sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="subtitle1" gutterBottom>
                    {project.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5, minHeight: 40 }}>
                    {project.description || 'No description provided.'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Created {project.createdAt ? new Date(project.createdAt).toLocaleDateString() : '—'}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
          {projects.length === 0 && (
            <Grid item xs={12}>
              <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
                <Typography color="text.secondary">
                  {organizationId ? 'No projects in this organization yet.' : 'Create or select an organization to get started.'}
                </Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      )}

      {/* Create Organization Dialog */}
      <Dialog open={orgDialogOpen} onClose={() => setOrgDialogOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>New organization</DialogTitle>
        <Box component="form" onSubmit={handleCreateOrg}>
          <DialogContent>
            <ErrorAlert message={orgError} onClose={() => setOrgError('')} />
            <TextField
              autoFocus
              fullWidth
              label="Organization name"
              value={orgName}
              onChange={(e) => setOrgName(e.target.value)}
              required
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOrgDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={orgSubmitting}>
              {orgSubmitting ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      {/* Create Project Dialog */}
      <Dialog open={projectDialogOpen} onClose={() => setProjectDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>New project</DialogTitle>
        <Box component="form" onSubmit={handleCreateProject}>
          <DialogContent>
            <ErrorAlert message={projectError} onClose={() => setProjectError('')} />
            <Stack spacing={2} sx={{ mt: 0.5 }}>
              <TextField
                autoFocus
                fullWidth
                label="Project name"
                value={projectForm.name}
                onChange={(e) => setProjectForm((f) => ({ ...f, name: e.target.value }))}
                required
              />
              <TextField
                fullWidth
                label="Description"
                value={projectForm.description}
                onChange={(e) => setProjectForm((f) => ({ ...f, description: e.target.value }))}
                multiline
                minRows={3}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setProjectDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={projectSubmitting}>
              {projectSubmitting ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
