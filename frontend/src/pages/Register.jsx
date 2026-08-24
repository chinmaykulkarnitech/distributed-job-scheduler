import { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Link,
  Stack,
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import ErrorAlert from '../components/ErrorAlert';
import AuthShell from '../components/AuthShell';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleChange = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await register(form.name, form.email, form.password);
      setSuccess(true);
      setTimeout(() => navigate('/login', { replace: true }), 900);
    } catch (err) {
      setError(err.friendlyMessage || 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell>
      <Paper elevation={0} sx={{ p: 4, width: '100%', maxWidth: 420, border: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h5" gutterBottom>
          Create your account
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Start scheduling and monitoring background jobs.
        </Typography>

        <ErrorAlert message={error} onClose={() => setError('')} />
        {success && <ErrorAlert message="Account created. Redirecting to login..." severity="success" />}

        <Box component="form" onSubmit={handleSubmit} noValidate>
          <Stack spacing={2}>
            <TextField
              label="Name"
              value={form.name}
              onChange={handleChange('name')}
              required
              fullWidth
              autoFocus
              autoComplete="name"
            />
            <TextField
              label="Email"
              type="email"
              value={form.email}
              onChange={handleChange('email')}
              required
              fullWidth
              autoComplete="email"
            />
            <TextField
              label="Password"
              type="password"
              value={form.password}
              onChange={handleChange('password')}
              required
              fullWidth
              helperText="At least 8 characters"
              autoComplete="new-password"
            />
            <Button type="submit" variant="contained" size="large" disabled={submitting} fullWidth>
              {submitting ? 'Creating account...' : 'Create account'}
            </Button>
          </Stack>
        </Box>

        <Typography variant="body2" sx={{ mt: 3, textAlign: 'center' }} color="text.secondary">
          Already have an account?{' '}
          <Link component={RouterLink} to="/login" underline="hover" fontWeight={600}>
            Sign in
          </Link>
        </Typography>
      </Paper>
    </AuthShell>
  );
}
