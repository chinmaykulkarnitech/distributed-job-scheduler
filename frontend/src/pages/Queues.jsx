import { useEffect, useState, useCallback } from 'react';
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
import PauseCircleOutlinedIcon from '@mui/icons-material/PauseCircleOutlined';
import PlayCircleOutlinedIcon from '@mui/icons-material/PlayCircleOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import RuleOutlinedIcon from '@mui/icons-material/RuleOutlined';
import { QueueAPI, RetryPolicyAPI } from '../services/api';
import OrgProjectQueuePicker from '../components/OrgProjectQueuePicker';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';
import StatusChip from '../components/StatusChip';

export default function Queues() {
  const [picker, setPicker] = useState({ organizationId: '', projectId: '' });
  const [queues, setQueues] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [retryPolicies, setRetryPolicies] = useState([]);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState({ name: '', priority: 5, concurrencyLimit: 1, retryPolicyId: '' });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  const [detailsQueue, setDetailsQueue] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState('');

  const [assignQueue, setAssignQueue] = useState(null);
  const [assignRetryPolicyId, setAssignRetryPolicyId] = useState('');
  const [assignSubmitting, setAssignSubmitting] = useState(false);
  const [assignError, setAssignError] = useState('');

  const loadQueues = useCallback(async (projectId) => {
    if (!projectId) {
      setQueues([]);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const { data } = await QueueAPI.getByProject(projectId);
      setQueues(data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load queues');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadQueues(picker.projectId);
  }, [picker.projectId, loadQueues]);

  useEffect(() => {
    RetryPolicyAPI.getAll()
      .then(({ data }) => setRetryPolicies(data || []))
      .catch(() => setRetryPolicies([]));
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      const { data } = await QueueAPI.create({
        projectId: picker.projectId,
        name: form.name,
        priority: Number(form.priority) || 0,
        concurrencyLimit: Number(form.concurrencyLimit) || 1,
        retryPolicyId: form.retryPolicyId || null,
      });
      setQueues((prev) => [data, ...prev]);
      setForm({ name: '', priority: 5, concurrencyLimit: 1, retryPolicyId: '' });
      setDialogOpen(false);
    } catch (err) {
      setFormError(err.friendlyMessage || 'Failed to create queue');
    } finally {
      setSubmitting(false);
    }
  };

  const toggleQueue = async (queue) => {
    setActionError('');
    try {
      const action = queue.status === 'PAUSED' ? QueueAPI.resume : QueueAPI.pause;
      const { data } = await action(queue.id);
      setQueues((prev) => prev.map((q) => (q.id === queue.id ? data : q)));
      setActionSuccess(`Queue "${queue.name}" ${data.status === 'PAUSED' ? 'paused' : 'resumed'}`);
    } catch (err) {
      setActionError(err.friendlyMessage || 'Failed to update queue');
    }
  };

  const openDetails = async (queue) => {
    setDetailsQueue(queue);
    setDetailsError('');
    setDetailsLoading(true);
    try {
      const { data } = await QueueAPI.getOne(queue.id);
      setDetailsQueue(data);
    } catch (err) {
      setDetailsError(err.friendlyMessage || 'Failed to load queue details');
    } finally {
      setDetailsLoading(false);
    }
  };

  const openAssignDialog = (queue) => {
    setAssignQueue(queue);
    setAssignRetryPolicyId(queue.retryPolicyId || '');
    setAssignError('');
  };

  const handleAssignRetryPolicy = async (e) => {
    e.preventDefault();
    if (!assignRetryPolicyId) {
      setAssignError('Select a retry policy to assign.');
      return;
    }
    setAssignError('');
    setAssignSubmitting(true);
    try {
      const { data } = await QueueAPI.assignRetryPolicy(assignQueue.id, assignRetryPolicyId);
      setQueues((prev) => prev.map((q) => (q.id === assignQueue.id ? data : q)));
      await loadQueues(picker.projectId);
      setActionSuccess(`Retry policy assigned to "${assignQueue.name}"`);
      setAssignQueue(null);
    } catch (err) {
      setAssignError(err.friendlyMessage || 'Failed to assign retry policy');
    } finally {
      setAssignSubmitting(false);
    }
  };

  const retryPolicyName = (id) => retryPolicies.find((rp) => rp.id === id)?.name;

  return (
    <Box>
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ sm: 'center' }}>
          <OrgProjectQueuePicker value={picker} onChange={setPicker} upTo="project" />
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh">
              <IconButton size="small" onClick={() => loadQueues(picker.projectId)}>
                <RefreshOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button
              variant="contained"
              startIcon={<AddOutlinedIcon />}
              disabled={!picker.projectId}
              onClick={() => setDialogOpen(true)}
            >
              Create Queue
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert message={error} onClose={() => setError('')} />
      <ErrorAlert message={actionError} onClose={() => setActionError('')} snackbar />
      <ErrorAlert message={actionSuccess} onClose={() => setActionSuccess('')} severity="success" snackbar />

      {loading ? (
        <Loading label="Loading queues..." />
      ) : (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Concurrency</TableCell>
                  <TableCell>Retry Policy</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {queues.map((q) => (
                  <TableRow key={q.id} hover>
                    <TableCell sx={{ fontWeight: 500 }}>{q.name}</TableCell>
                    <TableCell>
                      <StatusChip status={q.status} />
                    </TableCell>
                    <TableCell>{q.priority}</TableCell>
                    <TableCell>{q.concurrencyLimit}</TableCell>
                    <TableCell>
                      {q.retryPolicyId ? (
                        <Tooltip title={q.retryPolicyId}>
                          <Chip size="small" variant="outlined" label={retryPolicyName(q.retryPolicyId) || q.retryPolicyId.slice(0, 8)} />
                        </Tooltip>
                      ) : (
                        <Typography variant="caption" color="text.secondary">
                          none
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>{q.createdAt ? new Date(q.createdAt).toLocaleDateString() : '—'}</TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                        <Tooltip title="View details">
                          <IconButton size="small" onClick={() => openDetails(q)}>
                            <VisibilityOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Assign retry policy">
                          <IconButton size="small" onClick={() => openAssignDialog(q)}>
                            <RuleOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={q.status === 'PAUSED' ? 'Resume queue' : 'Pause queue'}>
                          <IconButton size="small" onClick={() => toggleQueue(q)}>
                            {q.status === 'PAUSED' ? (
                              <PlayCircleOutlinedIcon fontSize="small" />
                            ) : (
                              <PauseCircleOutlinedIcon fontSize="small" />
                            )}
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
                {queues.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                        {picker.projectId ? 'No queues in this project yet.' : 'Select a project to view its queues.'}
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>New queue</DialogTitle>
        <Box component="form" onSubmit={handleCreate}>
          <DialogContent>
            <ErrorAlert message={formError} onClose={() => setFormError('')} />
            <Stack spacing={2} sx={{ mt: 0.5 }}>
              <TextField
                autoFocus
                fullWidth
                label="Queue name"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                required
              />
              <Stack direction="row" spacing={2}>
                <TextField
                  fullWidth
                  type="number"
                  label="Priority"
                  value={form.priority}
                  onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value }))}
                  inputProps={{ min: 0 }}
                />
                <TextField
                  fullWidth
                  type="number"
                  label="Concurrency limit"
                  value={form.concurrencyLimit}
                  onChange={(e) => setForm((f) => ({ ...f, concurrencyLimit: e.target.value }))}
                  inputProps={{ min: 1 }}
                />
              </Stack>
              <TextField
                select
                fullWidth
                label="Retry policy (optional)"
                value={form.retryPolicyId}
                onChange={(e) => setForm((f) => ({ ...f, retryPolicyId: e.target.value }))}
              >
                <MenuItem value="">None</MenuItem>
                {retryPolicies.map((rp) => (
                  <MenuItem key={rp.id} value={rp.id}>
                    {rp.name} ({rp.strategy}, max {rp.maxAttempts})
                  </MenuItem>
                ))}
              </TextField>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={submitting}>
              {submitting ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      {/* Queue details dialog */}
      <Dialog open={Boolean(detailsQueue)} onClose={() => setDetailsQueue(null)} fullWidth maxWidth="xs">
        <DialogTitle>Queue details</DialogTitle>
        <DialogContent>
          <ErrorAlert message={detailsError} onClose={() => setDetailsError('')} />
          {detailsLoading ? (
            <Loading label="Loading queue..." />
          ) : (
            detailsQueue && (
              <Stack spacing={1.5}>
                <Box>
                  <Typography variant="caption" color="text.secondary">ID</Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}>
                    {detailsQueue.id}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Name</Typography>
                  <Typography variant="body2">{detailsQueue.name}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Status</Typography>
                  <Box sx={{ mt: 0.5 }}>
                    <StatusChip status={detailsQueue.status} />
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Project ID</Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}>
                    {detailsQueue.projectId}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Priority</Typography>
                  <Typography variant="body2">{detailsQueue.priority}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Concurrency limit</Typography>
                  <Typography variant="body2">{detailsQueue.concurrencyLimit}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Retry policy</Typography>
                  <Typography variant="body2">
                    {detailsQueue.retryPolicyId ? (retryPolicyName(detailsQueue.retryPolicyId) || detailsQueue.retryPolicyId) : 'None'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Created</Typography>
                  <Typography variant="body2">
                    {detailsQueue.createdAt ? new Date(detailsQueue.createdAt).toLocaleString() : '—'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Updated</Typography>
                  <Typography variant="body2">
                    {detailsQueue.updatedAt ? new Date(detailsQueue.updatedAt).toLocaleString() : '—'}
                  </Typography>
                </Box>
              </Stack>
            )
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsQueue(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Assign retry policy dialog */}
      <Dialog open={Boolean(assignQueue)} onClose={() => setAssignQueue(null)} fullWidth maxWidth="xs">
        <DialogTitle>Assign retry policy — {assignQueue?.name}</DialogTitle>
        <Box component="form" onSubmit={handleAssignRetryPolicy}>
          <DialogContent>
            <ErrorAlert message={assignError} onClose={() => setAssignError('')} />
            <TextField
              select
              fullWidth
              label="Retry policy"
              value={assignRetryPolicyId}
              onChange={(e) => setAssignRetryPolicyId(e.target.value)}
              helperText={retryPolicies.length === 0 ? 'No retry policies yet — create one on the Retry Policies page.' : ' '}
            >
              {retryPolicies.map((rp) => (
                <MenuItem key={rp.id} value={rp.id}>
                  {rp.name} ({rp.strategy}, max {rp.maxAttempts})
                </MenuItem>
              ))}
            </TextField>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setAssignQueue(null)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={assignSubmitting || retryPolicies.length === 0}>
              {assignSubmitting ? 'Assigning...' : 'Assign'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
