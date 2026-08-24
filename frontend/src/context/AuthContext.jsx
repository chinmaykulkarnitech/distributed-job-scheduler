import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  AuthAPI,
  getToken,
  getStoredUser,
  setAuth,
  clearAuth,
  registerUnauthorizedHandler,
} from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser());
  const [token, setToken] = useState(getToken());
  const [initializing, setInitializing] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    setInitializing(false);
  }, []);

  const logout = useCallback(
    (options = {}) => {
      clearAuth();
      setUser(null);
      setToken(null);
      if (!options.silent) {
        navigate('/login', { replace: true });
      }
    },
    [navigate]
  );

  // If any API call comes back 401/403, force the user back to login.
  useEffect(() => {
    registerUnauthorizedHandler(() => {
      setUser(null);
      setToken(null);
      navigate('/login', { replace: true });
    });
  }, [navigate]);

  const login = useCallback(async (email, password) => {
    const { data } = await AuthAPI.login({ email, password });
    const loggedInUser = {
      userId: data.userId,
      name: data.name,
      email: data.email,
    };
    setAuth(data.accessToken, loggedInUser);
    setToken(data.accessToken);
    setUser(loggedInUser);
    return loggedInUser;
  }, []);

  const register = useCallback(async (name, email, password) => {
    const { data } = await AuthAPI.register({ name, email, password });
    return data;
  }, []);

  const value = {
    user,
    token,
    isAuthenticated: Boolean(token),
    initializing,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
