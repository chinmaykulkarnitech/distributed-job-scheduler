import { AppBar, Toolbar, Typography, Box, Avatar, Chip } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { DRAWER_WIDTH } from './Sidebar';

function initials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join('');
}

export default function Navbar({ title }) {
  const { user } = useAuth();

  return (
    <AppBar
      position="fixed"
      sx={{
        width: `calc(100% - ${DRAWER_WIDTH}px)`,
        ml: `${DRAWER_WIDTH}px`,
      }}
      elevation={0}
    >
      <Toolbar sx={{ justifyContent: 'space-between' }}>
        <Typography variant="h6">{title}</Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Chip
            label="Backend: localhost:8080"
            size="small"
            variant="outlined"
            sx={{ display: { xs: 'none', md: 'flex' }, color: 'text.secondary', borderColor: 'divider' }}
          />
          <Box sx={{ textAlign: 'right', display: { xs: 'none', sm: 'block' } }}>
            <Typography variant="body2" fontWeight={600} lineHeight={1.2}>
              {user?.name || 'User'}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {user?.email}
            </Typography>
          </Box>
          <Avatar sx={{ bgcolor: 'primary.main', width: 36, height: 36, fontSize: 14 }}>
            {initials(user?.name) || 'U'}
          </Avatar>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
