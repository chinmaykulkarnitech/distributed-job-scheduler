import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Loading from './Loading';

export default function ProtectedRoute() {
  const { isAuthenticated, initializing } = useAuth();

  if (initializing) return <Loading fullScreen label="Checking session..." />;

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return <Outlet />;
}
