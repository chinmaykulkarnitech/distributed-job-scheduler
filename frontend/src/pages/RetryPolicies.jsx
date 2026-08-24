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
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import { RetryPolicyAPI } from '../services/api';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';

const STRATEGIES = ['FIXED', 'EXPONENTIAL'];

const EMPTY_FORM = {
  name: '',
  strategy: 'EXPONENTIAL',
  maxAttempts: 5,
  initialDelaySeconds: 10,
  maxDelaySeconds: 300,
};

export default function RetryPolicies() {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  // Create / edit dialog. `editingId` is null when creating.
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  // Details dialog
  const [detailsPolicy, setDetailsPolicy] = useState(null);

  // Delete confirmation
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const loadPolicies = useCallback(async () => {
    setError('');
    try {
      const { data } = await RetryPolicyAPI.getAll();
      setPolicies(data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load retry policies');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadPolicies();
  }, [loadPolicies]);

  const openCreateDialog = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setFormError('');
    setDialogOpen(true);
  };

  const openEditDialog = (policy) => {
    setEditingId(policy.id);
    setForm({
      name: policy.name,
      strategy: policy.strategy,
      maxAttempts: policy.maxAttempts,
      initialDelaySeconds: policy.initialDelaySeconds,
      maxDelaySeconds: policy.maxDelaySeconds,
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    const payload = {
      name: form.name,
      strategy: form.strategy,
      maxAttempts: Number(form.maxAttempts),
      initialDelaySeconds: Number(form.initialDelaySeconds),
      maxDelaySeconds: Number(form.maxDelaySeconds),
    };
    try {
      if (editingId) {
        const { data } = await RetryPolicyAPI.update(editingId, payload);
        setPolicies((prev) => prev.map((p) => (p.id === editingId ? data : p)));
        setActionSuccess('Retry policy updated');
      } else {
        const { data } = await RetryPolicyAPI.create(payload);
        setPolicies((prev) => [data, ...prev]);
        setActionSuccess('Retry policy created');
      }
      setDialogOpen(false);
    } catch (err) {
      setFormError(err.friendlyMessage || 'Failed to save retry policy');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleteError('');
    setDeleting(true);
    try {
      await RetryPolicyAPI.remove(deleteTarget.id);
      setPolicies((prev) => prev.filter((p) => p.id !== deleteTarget.id));
      setActionSuccess(`Retry policy "${deleteTarget.name}" deleted`);
      setDeleteTarget(null);
    } catch (err) {
      setDeleteError(err.friendlyMessage || 'Failed to delete retry policy');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <Box>
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ sm: 'center' }}>
          <Box>
            <Typography variant="subtitle1">Retry policies</Typography>
            <Typography variant="body2" color="text.secondary">
              Control how failed jobs are retried. Assign a policy to a queue from the Queues page.
            </Typography>
          </Box>
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh">
              <IconButton size="small" onClick={loadPolicies}>
                <RefreshOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button variant="contained" startIcon={<AddOutlinedIcon />} onClick={openCreateDialog}>
              Create Policy
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert message={error} onClose={() => setError('')} />
      <ErrorAlert message={actionSuccess} onClose={() => setActionSuccess('')} severity="success" snackbar />

      {loading ? (
        <Loading label="Loading retry policies..." />
      ) : (
        <Paper variant="outlined">
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Strategy</TableCell>
                  <TableCell>Max Attempts</TableCell>
                  <TableCell>Initial Delay (s)</TableCell>
                  <TableCell>Max Delay (s)</TableCell>
                  <TableCell>Updated</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {policies.map((p) => (
                  <TableRow key={p.id} hover>
                    <TableCell sx={{ fontWeight: 500 }}>{p.name}</TableCell>
                    <TableCell>
                      <Chip size="small" variant="outlined" label={p.strategy} />
                    </TableCell>
                    <TableCell>{p.maxAttempts}</TableCell>
                    <TableCell>{p.initialDelaySeconds}</TableCell>
                    <TableCell>{p.maxDelaySeconds}</TableCell>
                    <TableCell>{p.updatedAt ? new Date(p.updatedAt).toLocaleString() : '—'}</TableCell>
                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                        <Tooltip title="View details">
                          <IconButton size="small" onClick={() => setDetailsPolicy(p)}>
                            <VisibilityOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Edit">
                          <IconButton size="small" onClick={() => openEditDialog(p)}>
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton size="small" color="error" onClick={() => { setDeleteTarget(p); setDeleteError(''); }}>
                            <DeleteOutlineOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
                {policies.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                        No retry policies yet. Click &quot;Create Policy&quot; to define one.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Create / edit dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editingId ? 'Edit retry policy' : 'New retry policy'}</DialogTitle>
        <Box component="form" onSubmit={handleSubmit}>
          <DialogContent>
            <ErrorAlert message={formError} onClose={() => setFormError('')} />
            <Stack spacing={2} sx={{ mt: 0.5 }}>
              <TextField
                autoFocus
                fullWidth
                label="Policy name"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                required
              />
              <TextField
                select
                fullWidth
                label="Strategy"
                value={form.strategy}
                onChange={(e) => setForm((f) => ({ ...f, strategy: e.target.value }))}
              >
                {STRATEGIES.map((s) => (
                  <MenuItem key={s} value={s}>
                    {s}
                  </MenuItem>
                ))}
              </TextField>
              <Stack direction="row" spacing={2}>
                <TextField
                  fullWidth
                  type="number"
                  label="Max attempts"
                  value={form.maxAttempts}
                  onChange={(e) => setForm((f) => ({ ...f, maxAttempts: e.target.value }))}
                  inputProps={{ min: 1 }}
                  required
                />
                <TextField
                  fullWidth
                  type="number"
                  label="Initial delay (s)"
                  value={form.initialDelaySeconds}
                  onChange={(e) => setForm((f) => ({ ...f, initialDelaySeconds: e.target.value }))}
                  inputProps={{ min: 0 }}
                  required
                />
                <TextField
                  fullWidth
                  type="number"
                  label="Max delay (s)"
                  value={form.maxDelaySeconds}
                  onChange={(e) => setForm((f) => ({ ...f, maxDelaySeconds: e.target.value }))}
                  inputProps={{ min: 0 }}
                  required
                />
              </Stack>
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={submitting}>
              {submitting ? 'Saving...' : editingId ? 'Save changes' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      {/* Details dialog */}
      <Dialog open={Boolean(detailsPolicy)} onClose={() => setDetailsPolicy(null)} fullWidth maxWidth="xs">
        <DialogTitle>Retry policy details</DialogTitle>
        {detailsPolicy && (
          <DialogContent>
            <Stack spacing={1.5}>
              <Box>
                <Typography variant="caption" color="text.secondary">ID</Typography>
                <Typography variant="body2" sx={{ fontFamily: 'monospace', wordBreak: 'break-all' }}>
                  {detailsPolicy.id}
                </Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Name</Typography>
                <Typography variant="body2">{detailsPolicy.name}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Strategy</Typography>
                <Typography variant="body2">{detailsPolicy.strategy}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Max attempts</Typography>
                <Typography variant="body2">{detailsPolicy.maxAttempts}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Initial delay (s)</Typography>
                <Typography variant="body2">{detailsPolicy.initialDelaySeconds}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Max delay (s)</Typography>
                <Typography variant="body2">{detailsPolicy.maxDelaySeconds}</Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Created</Typography>
                <Typography variant="body2">
                  {detailsPolicy.createdAt ? new Date(detailsPolicy.createdAt).toLocaleString() : '—'}
                </Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">Updated</Typography>
                <Typography variant="body2">
                  {detailsPolicy.updatedAt ? new Date(detailsPolicy.updatedAt).toLocaleString() : '—'}
                </Typography>
              </Box>
            </Stack>
          </DialogContent>
        )}
        <DialogActions>
          <Button onClick={() => setDetailsPolicy(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Delete confirmation */}
      <Dialog open={Boolean(deleteTarget)} onClose={() => setDeleteTarget(null)} fullWidth maxWidth="xs">
        <DialogTitle>Delete retry policy?</DialogTitle>
        <DialogContent>
          <ErrorAlert message={deleteError} onClose={() => setDeleteError('')} />
          <Typography variant="body2">
            This will permanently delete <strong>{deleteTarget?.name}</strong>. Queues currently using this policy
            may be affected. This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteTarget(null)} disabled={deleting}>
            Cancel
          </Button>
          <Button color="error" variant="contained" onClick={handleDelete} disabled={deleting}>
            {deleting ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
