import api from './api';

export interface AuthResponse {
  token: string;
  driverId: number | null;
  riderId: number | null;
  userId: number;
}

export interface RegisterData {
  email: string;
  password: string;
  username: string;
}

export interface LoginData {
  email: string;
  password: string;
}

export async function registerRider(data: RegisterData): Promise<AuthResponse> {
  const response = await api.post('/auth/register/rider', data);
  return response.data;
}

export async function login(data: LoginData): Promise<AuthResponse> {
  const response = await api.post('/auth/authenticate', data);
  return response.data;
}
