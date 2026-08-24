import { useEffect, useState, useCallback, useMemo } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TableContainer,
  IconButton,
  Tooltip,
  LinearProgress,
} from '@mui/material';
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';
import HourglassEmptyOutlinedIcon from '@mui/icons-material/HourglassEmptyOutlined';
import PlayCircleOutlinedIcon from '@mui/icons-material/PlayCircleOutlined';
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import { JobAPI, WorkerAPI } from '../services/api';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';
import StatusChip from '../components/StatusChip';
import { Link as RouterLink } from 'react-router-dom';

function StatCard({ label, value, icon, tint }) {
  return (
    <Paper variant="outlined" sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 2, height: '100%' }}>
      <Box
        sx={{
          width: 44,
          height: 44,
          borderRadius: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: tint,
          color: 'white',
          flexShrink: 0,
        }}
      >
        {icon}
      </Box>
      <Box>
        <Typography variant="h5">{value}</Typography>
        <Typography variant="body2" color="text.secondary">
          {label}
        </Typography>
      </Box>
    </Paper>
  );
}

export default function Dashboard() {
  const [jobs, setJobs] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setError('');
    try {
      const [jobsRes, workersRes] = await Promise.all([JobAPI.getAll(), WorkerAPI.getOnline()]);
      setJobs(jobsRes.data || []);
      setWorkers(workersRes.data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    const interval = setInterval(load, 10000);
    return () => clearInterval(interval);
  }, [load]);

  const counts = useMemo(() => {
    const base = { QUEUED: 0, RUNNING: 0, COMPLETED: 0, FAILED: 0 };
    jobs.forEach((j) => {
      if (base[j.status] !== undefined) base[j.status] += 1;
    });
    return base;
  }, [jobs]);

  const recentJobs = useMemo(
    () =>
      [...jobs]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 8),
    [jobs]
  );

  if (loading) return <Loading label="Loading dashboard..." />;

  const total = jobs.length;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1 }}>
        <Tooltip title="Refresh">
          <IconButton onClick={load} size="small">
            <RefreshOutlinedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>

      <ErrorAlert message={error} onClose={() => setError('')} />

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard label="Total Jobs" value={total} icon={<WorkOutlineOutlinedIcon />} tint="#4F46E5" />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard label="Queued" value={counts.QUEUED} icon={<HourglassEmptyOutlinedIcon />} tint="#2563EB" />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard label="Running" value={counts.RUNNING} icon={<PlayCircleOutlinedIcon />} tint="#D97706" />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard label="Completed" value={counts.COMPLETED} icon={<CheckCircleOutlinedIcon />} tint="#16A34A" />
        </Grid>
        <Grid item xs={12} sm={6} md={2.4}>
          <StatCard label="Failed" value={counts.FAILED} icon={<ErrorOutlineOutlinedIcon />} tint="#DC2626" />
        </Grid>
      </Grid>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={4}>
          <StatCard label="Online Workers" value={workers.length} icon={<DnsOutlinedIcon />} tint="#0EA5A4" />
        </Grid>
        <Grid item xs={12} sm={8}>
          <Paper variant="outlined" sx={{ p: 2.5, height: '100%' }}>
            <Typography variant="subtitle2" sx={{ mb: 1.5 }}>
              Job status distribution
            </Typography>
            {total === 0 ? (
              <Typography variant="body2" color="text.secondary">
                No jobs yet.
              </Typography>
            ) : (
              ['QUEUED', 'RUNNING', 'COMPLETED', 'FAILED'].map((status) => (
                <Box key={status} sx={{ mb: 1.2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.4 }}>
                    <Typography variant="caption" color="text.secondary">
                      {status}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {counts[status]} / {total}
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={total ? (counts[status] / total) * 100 : 0}
                    sx={{ height: 6, borderRadius: 3 }}
                    color={
                      status === 'FAILED'
                        ? 'error'
                        : status === 'COMPLETED'
                        ? 'success'
                        : status === 'RUNNING'
                        ? 'warning'
                        : 'info'
                    }
                  />
                </Box>
              ))
            )}
          </Paper>
        </Grid>
      </Grid>

      <Grid container spacing={2}>
        <Grid item xs={12} lg={7}>
          <Paper variant="outlined">
            <Box sx={{ p: 2, pb: 0 }}>
              <Typography variant="subtitle2">Recent jobs</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Job Type</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Priority</TableCell>
                    <TableCell>Created</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {recentJobs.map((job) => (
                    <TableRow
                      key={job.id}
                      hover
                      component={RouterLink}
                      to={`/jobs/${job.id}`}
                      sx={{ textDecoration: 'none', cursor: 'pointer' }}
                    >
                      <TableCell sx={{ fontWeight: 500 }}>{job.jobType}</TableCell>
                      <TableCell>
                        <StatusChip status={job.status} />
                      </TableCell>
                      <TableCell>{job.priority}</TableCell>
                      <TableCell>{job.createdAt ? new Date(job.createdAt).toLocaleString() : '—'}</TableCell>
                    </TableRow>
                  ))}
                  {recentJobs.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4}>
                        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                          No jobs yet. Create one from the Jobs page.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} lg={5}>
          <Paper variant="outlined">
            <Box sx={{ p: 2, pb: 0 }}>
              <Typography variant="subtitle2">Worker status</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Active Jobs</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {workers.map((w) => (
                    <TableRow key={w.id}>
                      <TableCell sx={{ fontWeight: 500 }}>{w.name}</TableCell>
                      <TableCell>
                        <StatusChip status={w.status} />
                      </TableCell>
                      <TableCell>
                        {w.activeJobs} / {w.concurrencyLimit}
                      </TableCell>
                    </TableRow>
                  ))}
                  {workers.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={3}>
                        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                          No workers online.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
