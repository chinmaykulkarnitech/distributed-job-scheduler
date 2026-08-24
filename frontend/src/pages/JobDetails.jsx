import { useEffect, useState, useCallback } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Grid,
  Button,
  IconButton,
  Tooltip,
  Divider,
} from '@mui/material';
import ArrowBackOutlinedIcon from '@mui/icons-material/ArrowBackOutlined';
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined';
import { JobAPI, WorkerAPI } from '../services/api';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';
import StatusChip from '../components/StatusChip';

function Field({ label, value, mono = false }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 0.4 }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontFamily: mono ? 'monospace' : 'inherit', wordBreak: 'break-all' }}>
        {value ?? '—'}
      </Typography>
    </Box>
  );
}

function prettyJson(raw) {
  try {
    return JSON.stringify(JSON.parse(raw), null, 2);
  } catch {
    return raw;
  }
}

export default function JobDetails() {
  const { jobId } = useParams();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  const load = useCallback(async () => {
    setError('');
    try {
      const { data } = await JobAPI.getOne(jobId);
      setJob(data);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load job');
    } finally {
      setLoading(false);
    }
  }, [jobId]);

  useEffect(() => {
    load();
  }, [load]);

  const handleComplete = async () => {
    setActionError('');
    setActionSuccess('');
    try {
      await WorkerAPI.completeJob(job.claimedBy, job.id);
      setActionSuccess('Job marked complete');
      load();
    } catch (err) {
      setActionError(err.friendlyMessage || 'Failed to complete job');
    }
  };

  const handleFail = async () => {
    setActionError('');
    setActionSuccess('');
    try {
      await WorkerAPI.failJob(job.claimedBy, job.id);
      setActionSuccess('Job marked failed');
      load();
    } catch (err) {
      setActionError(err.friendlyMessage || 'Failed to fail job');
    }
  };

  if (loading) return <Loading label="Loading job..." />;

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <IconButton component={RouterLink} to="/jobs" size="small">
          <ArrowBackOutlinedIcon fontSize="small" />
        </IconButton>
        <Typography variant="subtitle1">Back to jobs</Typography>
        <Box sx={{ flexGrow: 1 }} />
        <Tooltip title="Refresh">
          <IconButton size="small" onClick={load}>
            <RefreshOutlinedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>

      <ErrorAlert message={error} onClose={() => setError('')} />
      <ErrorAlert message={actionError} onClose={() => setActionError('')} snackbar />
      <ErrorAlert message={actionSuccess} onClose={() => setActionSuccess('')} severity="success" snackbar />

      {job && (
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box>
              <Typography variant="h6">{job.jobType}</Typography>
              <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                {job.id}
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <StatusChip status={job.status} size="medium" />
              {job.status === 'RUNNING' && (
                <>
                  <Button size="small" variant="outlined" color="success" onClick={handleComplete}>
                    Complete
                  </Button>
                  <Button size="small" variant="outlined" color="error" onClick={handleFail}>
                    Fail
                  </Button>
                </>
              )}
            </Box>
          </Box>

          <Divider sx={{ mb: 3 }} />

          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Queue ID" value={job.queueId} mono />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Priority" value={job.priority} />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Attempt Count" value={job.attemptCount} />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Run At" value={job.runAt ? new Date(job.runAt).toLocaleString() : null} />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Claimed By" value={job.claimedBy} mono />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Claimed At" value={job.claimedAt ? new Date(job.claimedAt).toLocaleString() : null} />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Created At" value={job.createdAt ? new Date(job.createdAt).toLocaleString() : null} />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Updated At" value={job.updatedAt ? new Date(job.updatedAt).toLocaleString() : null} />
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Field label="Idempotency Key" value={job.idempotencyKey} mono />
            </Grid>
          </Grid>

          <Divider sx={{ my: 3 }} />

          <Typography variant="caption" color="text.secondary" sx={{ textTransform: 'uppercase', letterSpacing: 0.4 }}>
            Payload
          </Typography>
          <Box
            component="pre"
            sx={{
              mt: 1,
              p: 2,
              bgcolor: '#0F172A',
              color: '#E2E8F0',
              borderRadius: 2,
              fontSize: 13,
              overflowX: 'auto',
              fontFamily: 'monospace',
            }}
          >
            {prettyJson(job.payload)}
          </Box>
        </Paper>
      )}
    </Box>
  );
}
