import { Box, CircularProgress, Typography } from '@mui/material';

export default function Loading({ label = 'Loading...', fullScreen = false, size = 32 }) {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        py: fullScreen ? 0 : 6,
        height: fullScreen ? '100vh' : 'auto',
        width: '100%',
      }}
    >
      <CircularProgress size={size} thickness={4} />
      {label && (
        <Typography variant="body2" color="text.secondary">
          {label}
        </Typography>
      )}
    </Box>
  );
}
