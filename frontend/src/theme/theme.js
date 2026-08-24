import { createTheme } from '@mui/material/styles';

// A quiet, professional "ops console" palette: deep slate for structure,
// an indigo accent for primary actions, and desaturated status colors so
// the job/worker status chips are what actually draws the eye.
const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#4F46E5',
      dark: '#4338CA',
      light: '#818CF8',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#0EA5A4',
    },
    background: {
      default: '#F6F7FB',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#1E2233',
      secondary: '#6B7280',
    },
    divider: '#E5E7EB',
    success: { main: '#16A34A', light: '#DCFCE7', dark: '#15803D' },
    warning: { main: '#D97706', light: '#FEF3C7', dark: '#B45309' },
    error: { main: '#DC2626', light: '#FEE2E2', dark: '#B91C1C' },
    info: { main: '#2563EB', light: '#DBEAFE', dark: '#1D4ED8' },
  },
  shape: {
    borderRadius: 10,
  },
  typography: {
    fontFamily: '"Inter", "Segoe UI", Roboto, sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.02em' },
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 600 },
    button: { fontWeight: 600, textTransform: 'none' },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8 },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#FFFFFF',
          color: '#1E2233',
          boxShadow: '0 1px 0 0 #E5E7EB',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 600 },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 700, color: '#6B7280', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.03em' },
      },
    },
  },
});

export default theme;
