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
  Stack,
  IconButton,
  Tooltip,
  Chip,
  LinearProgress,
} from '@mui/material';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined';
import FavoriteOutlinedIcon from '@mui/icons-material/FavoriteOutlined';
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined';
import { WorkerAPI } from '../services/api';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';
import StatusChip from '../components/StatusChip';

const POLL_MS = 10000;

export default function Workers() {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  const [registerOpen, setRegisterOpen] = useState(false);
  const [registerName, setRegisterName] = useState('');
  const [registerSubmitting, setRegisterSubmitting] = useState(false);
  const [registerError, setRegisterError] = useState('');

  const [heartbeatWorker, setHeartbeatWorker] = useState(null);
  const [heartbeatForm, setHeartbeatForm] = useState({ cpuUsage: '', memoryUsage: '', activeJobs: 0 });
  const [heartbeatSubmitting, setHeartbeatSubmitting] = useState(false);
  const [heartbeatError, setHeartbeatError] = useState('');

  const [lookupId, setLookupId] = useState('');
  const [lookupError, setLookupError] = useState('');
  const [lookupResult, setLookupResult] = useState(null);
  const [lookupLoading, setLookupLoading] = useState(false);

  const loadWorkers = useCallback(async () => {
    setError('');
    try {
      const { data } = await WorkerAPI.getOnline();
      setWorkers(data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load workers');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadWorkers();
    const interval = setInterval(loadWorkers, POLL_MS);
    return () => clearInterval(interval);
  }, [loadWorkers]);

  const handleRegister = async (e) => {
    e.preventDefault();
    setRegisterError('');
    setRegisterSubmitting(true);
    try {
      const { data } = await WorkerAPI.register(registerName);
      setWorkers((prev) => [data, ...prev]);
      setRegisterName('');
      setRegisterOpen(false);
      setActionSuccess(`Worker "${data.name}" registered`);
    } catch (err) {
      setRegisterError(err.friendlyMessage || 'Failed to register worker');
    } finally {
      setRegisterSubmitting(false);
    }
  };

  const openHeartbeatDialog = (worker) => {
    setHeartbeatWorker(worker);
    setHeartbeatForm({ cpuUsage: '', memoryUsage: '', activeJobs: worker.activeJobs ?? 0 });
    setHeartbeatError('');
  };

  const handleHeartbeat = async (e) => {
    e.preventDefault();
    setHeartbeatError('');
    setHeartbeatSubmitting(true);
    try {
      await WorkerAPI.heartbeat(heartbeatWorker.id, {
        cpuUsage: heartbeatForm.cpuUsage === '' ? undefined : Number(heartbeatForm.cpuUsage),
        memoryUsage: heartbeatForm.memoryUsage === '' ? undefined : Number(heartbeatForm.memoryUsage),
        activeJobs: Number(heartbeatForm.activeJobs) || 0,
      });
      setActionSuccess(`Heartbeat sent for "${heartbeatWorker.name}"`);
      setHeartbeatWorker(null);
      loadWorkers();
    } catch (err) {
      setHeartbeatError(err.friendlyMessage || 'Failed to send heartbeat');
    } finally {
      setHeartbeatSubmitting(false);
    }
  };

  const handleLookup = async (e) => {
    e.preventDefault();
    if (!lookupId.trim()) return;
    setLookupError('');
    setLookupResult(null);
    setLookupLoading(true);
    try {
      const { data } = await WorkerAPI.getOne(lookupId.trim());
      setLookupResult(data);
    } catch (err) {
      setLookupError(err.friendlyMessage || 'Worker not found');
    } finally {
      setLookupLoading(false);
    }
  };

  return (
    <Box>
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ md: 'center' }}>
          <Box component="form" onSubmit={handleLookup}>
            <Stack direction="row" spacing={1.5}>
              <TextField
                size="small"
                label="Look up worker by ID"
                value={lookupId}
                onChange={(e) => setLookupId(e.target.value)}
                sx={{ minWidth: 280 }}
              />
              <Button type="submit" variant="outlined" startIcon={<SearchOutlinedIcon />} disabled={lookupLoading}>
                Look up
              </Button>
            </Stack>
          </Box>
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh">
              <IconButton size="small" onClick={loadWorkers}>
                <RefreshOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button variant="contained" startIcon={<AddOutlinedIcon />} onClick={() => setRegisterOpen(true)}>
              Register Worker
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert message={error} onClose={() => setError('')} />
      <ErrorAlert message={actionError} onClose={() => setActionError('')} snackbar />
      <ErrorAlert message={actionSuccess} onClose={() => setActionSuccess('')} severity="success" snackbar />

      {lookupError && (
        <ErrorAlert message={lookupError} onClose={() => setLookupError('')} />
      )}
      {lookupResult && (
        <Paper variant="outlined" sx={{ p: 2, mb: 3, borderColor: 'primary.main' }}>
          <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
            <Typography variant="subtitle2">Lookup result:</Typography>
            <Typography variant="body2" fontWeight={600}>
              {lookupResult.name}
            </Typography>
            <StatusChip status={lookupResult.status} />
            <Typography variant="caption" color="text.secondary">
              Last heartbeat:{' '}
              {lookupResult.lastHeartbeatAt ? new Date(lookupResult.lastHeartbeatAt).toLocaleString() : '—'}
            </Typography>
            <Chip size="small" variant="outlined" label={`${lookupResult.activeJobs}/${lookupResult.concurrencyLimit} active`} />
          </Stack>
        </Paper>
      )}

      {loading ? (
        <Loading label="Loading workers..." />
      ) : (
        <Paper variant="outlined">
          <Box sx={{ p: 2, pb: 0 }}>
            <Typography variant="subtitle2">Online workers</Typography>
          </Box>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Hostname</TableCell>
                  <TableCell>Last Heartbeat</TableCell>
                  <TableCell>Started At</TableCell>
                  <TableCell>Concurrency</TableCell>
                  <TableCell>Active Jobs</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {workers.map((w) => (
                  <TableRow key={w.id} hover>
                    <TableCell sx={{ fontWeight: 500 }}>{w.name}</TableCell>
                    <TableCell>
                      <StatusChip status={w.status} />
                    </TableCell>
                    <TableCell>{w.hostname || '—'}</TableCell>
                    <TableCell>{w.lastHeartbeatAt ? new Date(w.lastHeartbeatAt).toLocaleString() : '—'}</TableCell>
                    <TableCell>{w.startedAt ? new Date(w.startedAt).toLocaleString() : '—'}</TableCell>
                    <TableCell>{w.concurrencyLimit}</TableCell>
                    <TableCell sx={{ minWidth: 120 }}>
                      <Typography variant="caption">
                        {w.activeJobs} / {w.concurrencyLimit}
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={w.concurrencyLimit ? (w.activeJobs / w.concurrencyLimit) * 100 : 0}
                        sx={{ height: 5, borderRadius: 3, mt: 0.5 }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Tooltip title="Send heartbeat">
                        <IconButton size="small" onClick={() => openHeartbeatDialog(w)}>
                          <FavoriteOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
                {workers.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8}>
                      <Typography variant="body2" color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                        No workers online. Register one to get started.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Register worker dialog */}
      <Dialog open={registerOpen} onClose={() => setRegisterOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Register worker</DialogTitle>
        <Box component="form" onSubmit={handleRegister}>
          <DialogContent>
            <ErrorAlert message={registerError} onClose={() => setRegisterError('')} />
            <TextField
              autoFocus
              fullWidth
              label="Worker name"
              placeholder="worker-A"
              value={registerName}
              onChange={(e) => setRegisterName(e.target.value)}
              required
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setRegisterOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={registerSubmitting}>
              {registerSubmitting ? 'Registering...' : 'Register'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      {/* Heartbeat dialog */}
      <Dialog open={Boolean(heartbeatWorker)} onClose={() => setHeartbeatWorker(null)} fullWidth maxWidth="xs">
        <DialogTitle>Send heartbeat — {heartbeatWorker?.name}</DialogTitle>
        <Box component="form" onSubmit={handleHeartbeat}>
          <DialogContent>
            <ErrorAlert message={heartbeatError} onClose={() => setHeartbeatError('')} />
            <Stack spacing={2}>
              <TextField
                fullWidth
                type="number"
                label="CPU usage %"
                value={heartbeatForm.cpuUsage}
                onChange={(e) => setHeartbeatForm((f) => ({ ...f, cpuUsage: e.target.value }))}
                inputProps={{ min: 0, max: 100, step: 0.1 }}
              />
              <TextField
                fullWidth
                type="number"
                label="Memory usage %"
                value={heartbeatForm.memoryUsage}
                onChange={(e) => setHeartbeatForm((f) => ({ ...f, memoryUsage: e.target.value }))}
                inputProps={{ min: 0, max: 100, step: 0.1 }}
              />
              <TextField
                fullWidth
                type="number"
                label="Active jobs"
                value={heartbeatForm.activeJobs}
                onChange={(e) => setHeartbeatForm((f) => ({ ...f, activeJobs: e.target.value }))}
                inputProps={{ min: 0 }}
              />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setHeartbeatWorker(null)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={heartbeatSubmitting}>
              {heartbeatSubmitting ? 'Sending...' : 'Send heartbeat'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
