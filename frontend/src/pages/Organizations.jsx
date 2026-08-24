import { useEffect, useState, useCallback } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Stack,
  IconButton,
  Tooltip,
  Avatar,
} from '@mui/material';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined';
import CorporateFareOutlinedIcon from '@mui/icons-material/CorporateFareOutlined';
import { OrganizationAPI } from '../services/api';
import Loading from '../components/Loading';
import ErrorAlert from '../components/ErrorAlert';

export default function Organizations() {
  const [organizations, setOrganizations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [dialogOpen, setDialogOpen] = useState(false);
  const [name, setName] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  const loadOrganizations = useCallback(async () => {
    setError('');
    try {
      const { data } = await OrganizationAPI.getMine();
      setOrganizations(data || []);
    } catch (err) {
      setError(err.friendlyMessage || 'Failed to load organizations');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadOrganizations();
  }, [loadOrganizations]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      const { data } = await OrganizationAPI.create({ name });
      setOrganizations((prev) => [data, ...prev]);
      setName('');
      setDialogOpen(false);
    } catch (err) {
      setFormError(err.friendlyMessage || 'Failed to create organization');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="space-between" alignItems={{ sm: 'center' }}>
          <Box>
            <Typography variant="subtitle1">Your organizations</Typography>
            <Typography variant="body2" color="text.secondary">
              Organizations are the top-level container for your projects, queues, and jobs.
            </Typography>
          </Box>
          <Stack direction="row" spacing={1}>
            <Tooltip title="Refresh">
              <IconButton size="small" onClick={loadOrganizations}>
                <RefreshOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Button variant="contained" startIcon={<AddOutlinedIcon />} onClick={() => setDialogOpen(true)}>
              Create Organization
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <ErrorAlert message={error} onClose={() => setError('')} />

      {loading ? (
        <Loading label="Loading organizations..." />
      ) : (
        <Grid container spacing={2}>
          {organizations.map((org) => (
            <Grid item xs={12} sm={6} md={4} key={org.id}>
              <Card variant="outlined" sx={{ height: '100%' }}>
                <CardContent>
                  <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 1.5 }}>
                    <Avatar sx={{ bgcolor: 'primary.main', width: 36, height: 36 }}>
                      <CorporateFareOutlinedIcon fontSize="small" />
                    </Avatar>
                    <Typography variant="subtitle1" sx={{ wordBreak: 'break-word' }}>
                      {org.name}
                    </Typography>
                  </Stack>
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontFamily: 'monospace' }}>
                    {org.id}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Created {org.createdAt ? new Date(org.createdAt).toLocaleDateString() : '—'}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
          {organizations.length === 0 && (
            <Grid item xs={12}>
              <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
                <Typography color="text.secondary">
                  You don&apos;t belong to any organizations yet. Create one to get started.
                </Typography>
              </Paper>
            </Grid>
          )}
        </Grid>
      )}

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>New organization</DialogTitle>
        <Box component="form" onSubmit={handleCreate}>
          <DialogContent>
            <ErrorAlert message={formError} onClose={() => setFormError('')} />
            <TextField
              autoFocus
              fullWidth
              label="Organization name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button type="submit" variant="contained" disabled={submitting}>
              {submitting ? 'Creating...' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
}
