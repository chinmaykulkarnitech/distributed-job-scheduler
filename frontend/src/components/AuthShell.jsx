import { Box, Typography } from '@mui/material';

export default function AuthShell({ children }) {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        backgroundImage:
          'radial-gradient(circle at 15% 15%, rgba(79,70,229,0.10), transparent 40%), radial-gradient(circle at 85% 85%, rgba(14,165,164,0.10), transparent 40%)',
        p: 2,
      }}
    >
      <Box sx={{ position: 'fixed', top: 28, left: 32, display: 'flex', alignItems: 'center', gap: 1.2 }}>
        <Box
          sx={{
            width: 28,
            height: 28,
            borderRadius: '8px',
            background: 'linear-gradient(135deg, #4F46E5, #0EA5A4)',
          }}
        />
        <Typography variant="subtitle1">Job Scheduler</Typography>
      </Box>
      {children}
    </Box>
  );
}
