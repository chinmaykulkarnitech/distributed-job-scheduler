import { Box, Toolbar } from '@mui/material';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

const TITLES = {
  '/dashboard': 'Dashboard',
  '/organizations': 'Organizations',
  '/projects': 'Projects',
  '/queues': 'Queues',
  '/jobs': 'Jobs',
  '/workers': 'Workers',
  '/retry-policies': 'Retry Policies',
};

function titleFor(pathname) {
  if (TITLES[pathname]) return TITLES[pathname];
  if (pathname.startsWith('/jobs/')) return 'Job Details';
  return 'Job Scheduler';
}

export default function AppLayout() {
  const location = useLocation();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Sidebar />
      <Navbar title={titleFor(location.pathname)} />
      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 3 } }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
