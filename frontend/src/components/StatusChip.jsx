import { Chip } from '@mui/material';

const STATUS_STYLES = {
  QUEUED: { color: 'info' },
  RUNNING: { color: 'warning' },
  COMPLETED: { color: 'success' },
  FAILED: { color: 'error' },
  ONLINE: { color: 'success' },
  OFFLINE: { color: 'default' },
  ACTIVE: { color: 'success' },
  PAUSED: { color: 'warning' },
};

export default function StatusChip({ status, size = 'small' }) {
  const style = STATUS_STYLES[status] || { color: 'default' };
  return (
    <Chip
      label={status || 'UNKNOWN'}
      color={style.color}
      size={size}
      variant={style.color === 'default' ? 'outlined' : 'filled'}
      sx={style.color === 'default' ? {} : { color: (t) => t.palette[style.color]?.dark, bgcolor: (t) => t.palette[style.color]?.light }}
    />
  );
}
