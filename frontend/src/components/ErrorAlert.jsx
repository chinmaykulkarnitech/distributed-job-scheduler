import { Alert, Snackbar } from '@mui/material';

/**
 * Two usage modes:
 *  - Inline: <ErrorAlert message={error} /> renders an Alert in place.
 *  - Snackbar: <ErrorAlert message={error} onClose={...} snackbar /> renders
 *    a transient toast, useful for dialog/action errors (e.g. 409 conflicts).
 */
export default function ErrorAlert({ message, onClose, severity = 'error', snackbar = false }) {
  if (!message) return null;

  if (snackbar) {
    return (
      <Snackbar
        open={Boolean(message)}
        autoHideDuration={6000}
        onClose={onClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={onClose} severity={severity} variant="filled" sx={{ width: '100%' }}>
          {message}
        </Alert>
      </Snackbar>
    );
  }

  return (
    <Alert severity={severity} onClose={onClose} sx={{ mb: 2 }}>
      {message}
    </Alert>
  );
}
