import { NavLink } from 'react-router-dom';
import {
  Box,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Divider,
} from '@mui/material';
import DashboardOutlinedIcon from '@mui/icons-material/DashboardOutlined';
import CorporateFareOutlinedIcon from '@mui/icons-material/CorporateFareOutlined';
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import ViewListOutlinedIcon from '@mui/icons-material/ViewListOutlined';
import WorkOutlineOutlinedIcon from '@mui/icons-material/WorkOutlineOutlined';
import MemoryOutlinedIcon from '@mui/icons-material/MemoryOutlined';
import RuleOutlinedIcon from '@mui/icons-material/RuleOutlined';
import LogoutOutlinedIcon from '@mui/icons-material/LogoutOutlined';
import { useAuth } from '../context/AuthContext';

export const DRAWER_WIDTH = 232;

const navItems = [
  { label: 'Dashboard', path: '/dashboard', icon: <DashboardOutlinedIcon /> },
  { label: 'Organizations', path: '/organizations', icon: <CorporateFareOutlinedIcon /> },
  { label: 'Projects', path: '/projects', icon: <FolderOutlinedIcon /> },
  { label: 'Queues', path: '/queues', icon: <ViewListOutlinedIcon /> },
  { label: 'Jobs', path: '/jobs', icon: <WorkOutlineOutlinedIcon /> },
  { label: 'Workers', path: '/workers', icon: <MemoryOutlinedIcon /> },
  { label: 'Retry Policies', path: '/retry-policies', icon: <RuleOutlinedIcon /> },
];

export default function Sidebar() {
  const { logout } = useAuth();

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        [`& .MuiDrawer-paper`]: {
          width: DRAWER_WIDTH,
          boxSizing: 'border-box',
          borderRight: '1px solid',
          borderColor: 'divider',
          backgroundColor: 'background.paper',
        },
      }}
    >
      <Toolbar sx={{ px: 3 }}>
        <Box
          sx={{
            width: 30,
            height: 30,
            borderRadius: '8px',
            background: 'linear-gradient(135deg, #4F46E5, #0EA5A4)',
            mr: 1.5,
            flexShrink: 0,
          }}
        />
        <Typography variant="subtitle1" noWrap>
          Job Scheduler
        </Typography>
      </Toolbar>
      <Divider />
      <List sx={{ px: 1.5, py: 2, flexGrow: 1 }}>
        {navItems.map((item) => (
          <ListItemButton
            key={item.path}
            component={NavLink}
            to={item.path}
            sx={{
              borderRadius: 2,
              mb: 0.5,
              '&.active': {
                backgroundColor: 'primary.main',
                color: 'primary.contrastText',
                '& .MuiListItemIcon-root': { color: 'primary.contrastText' },
                '&:hover': { backgroundColor: 'primary.dark' },
              },
            }}
          >
            <ListItemIcon sx={{ minWidth: 40 }}>{item.icon}</ListItemIcon>
            <ListItemText
              primary={item.label}
              primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }}
            />
          </ListItemButton>
        ))}
      </List>
      <Divider />
      <List sx={{ px: 1.5, py: 1.5 }}>
        <ListItemButton onClick={() => logout()} sx={{ borderRadius: 2 }}>
          <ListItemIcon sx={{ minWidth: 40 }}>
            <LogoutOutlinedIcon />
          </ListItemIcon>
          <ListItemText primary="Logout" primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }} />
        </ListItemButton>
      </List>
    </Drawer>
  );
}
