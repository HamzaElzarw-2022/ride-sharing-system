import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from 'react';
import type { AuthResponse } from '../services/authService';
import api, { setupInterceptors } from '../services/api';

interface AuthContextType {
  auth: AuthResponse | null;
  login: (data: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [auth, setAuth] = useState<AuthResponse | null>(null);

  const logout = useCallback(() => {
    setAuth(null);
    localStorage.removeItem('auth');
    delete api.defaults.headers.common['Authorization'];
  }, []);

  useEffect(() => {
    setupInterceptors(logout);
  }, [logout]);

  useEffect(() => {
    const storedAuth = localStorage.getItem('auth');
    if (storedAuth) {
      const authData = JSON.parse(storedAuth);
      setAuth(authData);
      api.defaults.headers.common['Authorization'] = `Bearer ${authData.token}`;
    }
  }, []);

  const login = (data: AuthResponse) => {
    setAuth(data);
    localStorage.setItem('auth', JSON.stringify(data));
    api.defaults.headers.common['Authorization'] = `Bearer ${data.token}`;
  };

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
