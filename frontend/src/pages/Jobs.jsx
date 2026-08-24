import { useEffect, useState, useCallback, useMemo } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TableContainer,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Stack,
  IconButton,
  Tooltip,
  Chip,
} from '@mui/material';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import BoltOutlinedIcon from '@mui/icons-material/BoltOutlined';
import CheckOutlinedIcon from '@mui/icons-material/CheckOutlined';
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined';
import { JobAPI, WorkerAPI } from '../services/api';
import OrgProjectQueuePicker from '../components/OrgProjectQueuePicker';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';
import StatusChip from '../components/StatusChip';

const POLL_MS = 10000;

export default function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  const [workers, setWorkers] = useState([]);
  const [actingWorkerId, setActingWorkerId] = useState('');
  const [claiming, setClaiming] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [createPicker, setCreatePicker] = useState({ organizationId: '', projectId: '', queueId: '' });
  const [createForm, setCreateForm] = useState({
    jobType: '',
    priority: 5,
    payload: '{\n  \n}',
    runAt: '',
    idempotencyKey: '',
  });
  const [createSubmitting, setCreateSubmitting] = useState(false);
  const [createError, setCreateError] = useState('');

  const loadJobs = useCallback(async () => {
    setError('');
    try {
      const { data } = await JobAPI.getAll();
      setJobs(data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load jobs');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadWorkers = useCallback(async () => {
    try {
      const { data } = await WorkerAPI.getOnline();
      setWorkers(data || []);
      setActingWorkerId((prev) => prev || data?.[0]?.id || '');
    } catch {
      // Non-fatal: worker pool just won't be available for claiming.
    }
  }, []);

  useEffect(() => {
    loadJobs();
    loadWorkers();
    const interval = setInterval(loadJobs, POLL_MS);
    return () => clearInterval(interval);
  }, [loadJobs, loadWorkers]);

  const sortedJobs = useMemo(
    () => [...jobs].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)),
    [jobs]
  );

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreateError('');

    if (!createPicker.queueId) {
      setCreateError('Select an organization, project and queue for this job.');
      return;
    }

    try {
      JSON.parse(createForm.payload);
    } catch {
      setCreateError('Payload must be valid JSON.');
      return;
    }

    setCreateSubmitting(true);
    try {
      const { data } = await JobAPI.create({
        queueId: createPicker.queueId,
        jobType: createForm.jobType,
        priority: Number(createForm.priority) || 0,
        payload: createForm.payload,
        runAt: createForm.runAt ? new Date(createForm.runAt).toISOString().slice(0, 19) : null,
        idempotencyKey: createForm.idempotencyKey || null,
      });
      setJobs((prev) => [data, ...prev]);
      setCreateOpen(false);
      setCreateForm({ jobType: '', priority: 5, payload: '{\n  \n}', runAt: '', idempotencyKey: '' });
    } catch (err) {
      setCreateError(err.friendlyMessage || 'Failed to create job');
    } finally {
      setCreateSubmitting(false);
    }
  };

  const handleClaimNext = async () => {
    if (!actingWorkerId) {
      setActionError('Register or select an online worker on the Workers page first.');
      return;
    }
    setActionError('');
    setActionSuccess('');
    setClaiming(true);
    try {
      const { data } = await WorkerAPI.claimJob(actingWorkerId);
      setActionSuccess(`Claimed job "${data.jobType}" (${data.id.slice(0, 8)}...)`);
      loadJobs();
    } catch (err) {
      setActionError(err.friendlyMessage || 'No job available to claim');
    } finally {
      setClaiming(false);
    }
  };

  const handleComplete = async (job) => {
    setActionError('');
    setActionSuccess('');
    try {
      await WorkerAPI.completeJob(job.claimedBy, job.id);
      setActionSuccess(`Job ${job.id.slice(0, 8)}... marked complete`);
      loadJobs();
    } catch (err) {
      setActionError(err.friendlyMessage || 'Failed to complete job');
    }
  };

  const handleFail = async (job) => {
    setActionError('');
    setActionSuccess('');
    try {
      await WorkerAPI.failJob(job.claimedBy, job.id);
      setActionSuccess(`Job ${job.id.slice(0, 8)}... marked failed`);
      loadJobs();
    } catch (err) {
      setActionError(err.friendlyMessage || 'Failed to fail job');
    }
  };

  return (
    <Box>
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ md: 'center' }}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5} alignItems={{ sm: 'center' }}>
            <TextField
              select
              size="small"
              label="Acting worker"
              value={actingWorkerId}
              onChange={(e) => setActingWorkerId(e.target.value)}
              sx={{ minWidth: 220 }}
              helperText={workers.length === 0 ? 'No online workers — register one first' : ' '}
            >
              {workers.map((w) => (
                <MenuItem key={w.id} value={w.id}>
                  {w.name}
                </MenuItem>
              ))}
            </TextField>
            <Button
              variant="outlined"
              startIcon={<BoltOutlinedIcon />}
              onClick={handleClaimNext}
              disabled={claiming || !actingWorkerId}
            >
              {claiming ? 'Claiming...' : 'Claim Next Job'}
            </Button>
          </Stack>
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh">
              <IconButton size="small" onClick={loadJobs}>
                <RefreshOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button variant="contained" startIcon={<AddOutlinedIcon />} onClick={() => setCreateOpen(true)}>
              Create Job
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert message={error} onClose={() => setError('')} />
      <ErrorAlert message={actionError} onClose={() => setActionError('')} snackbar />
      <ErrorAlert message={actionSuccess} onClose={() => setActionSuccess('')} severity="success" snackbar />

      {loading ? (
        <Loading label="Loading jobs..." />
      ) : (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Job ID</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Queue</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Attempts</TableCell>
                  <TableCell>Run At</TableCell>
                  <TableCell>Claimed By</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {sortedJobs.map((job) => (
                  <TableRow key={job.id} hover>
                    <TableCell>
                      <Tooltip title={job.id}>
                        <Chip size="small" variant="outlined" label={job.id.slice(0, 8)} />
                      </Tooltip>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 500 }}>{job.jobType}</TableCell>
                    <TableCell>
                      <Tooltip title={job.queueId}>
                        <span>{job.queueId?.slice(0, 8)}...</span>
                      </Tooltip>
                    </TableCell>
                    <TableCell>
                      <StatusChip status={job.status} />
                    </TableCell>
                    <TableCell>{job.priority}</TableCell>
                    <TableCell>{job.attemptCount ?? 0}</TableCell>
                    <TableCell>{job.runAt ? new Date(job.runAt).toLocaleString() : '—'}</TableCell>
                    <TableCell>{job.claimedBy ? `${job.claimedBy.slice(0, 8)}...` : '—'}</TableCell>
                    <TableCell>{job.createdAt ? new Date(job.createdAt).toLocaleString() : '—'}</TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                        {job.status === 'RUNNING' && (
                          <>
                            <Tooltip title="Complete job">
                              <IconButton size="small" color="success" onClick={() => handleComplete(job)}>
                                <CheckOutlinedIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="Fail job">
                              <IconButton size="small" color="error" onClick={() => handleFail(job)}>
                                <CloseOutlinedIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </>
                        )}
                        <Tooltip title="View details">
                          <IconButton size="small" component={RouterLink} to={`/jobs/${job.id}`}>
                            <VisibilityOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
                {sortedJobs.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={10}>
                      <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                        No jobs yet. Click &quot;Create Job&quot; to get started.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>New job</DialogTitle>
        <Box component="form" onSubmit={handleCreate}>
          <DialogContent>
            <ErrorAlert message={createError} onClose={() => setCreateError('')} />
            <Stack spacing={2} sx={{ mt: 0.5 }}>
              <OrgProjectQueuePicker value={createPicker} onChange={setCreatePicker} />
              <TextField
                fullWidth
                label="Job type"
                placeholder="EMAIL"
                value={createForm.jobType}
                onChange={(e) => setCreateForm((f) => ({ ...f, jobType: e.target.value }))}
                required
              />
              <TextField
                fullWidth
                type="number"
                label="Priority"
                value={createForm.priority}
                onChange={(e) => setCreateForm((f) => ({ ...f, priority: e.target.value }))}
                inputProps={{ min: 0 }}
              />
              <TextField
                fullWidth
                label="Payload (JSON)"
                value={createForm.payload}
                onChange={(e) => setCreateForm((f) => ({ ...f, payload: e.target.value }))}
                multiline
                minRows={4}
                required
                sx={{ '& textarea': { fontFamily: 'monospace', fontSize: 13 } }}
              />
              <TextField
                fullWidth
                type="datetime-local"
                label="Run at (optional)"
                InputLabelProps={{ shrink: true }}
                value={createForm.runAt}
                onChange={(e) => setCreateForm((f) => ({ ...f, runAt: e.target.value }))}
              />
              <TextField
                fullWidth
                label="Idempotency key (optional)"
                value={createForm.idempotencyKey}
                onChange={(e) => setCreateForm((f) => ({ ...f, idempotencyKey: e.target.value }))}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={createSubmitting}>
              {createSubmitting ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
