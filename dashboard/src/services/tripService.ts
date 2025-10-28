import api from './api';
import type { Point } from './routeService';

export interface Trip {
  id: number;
  status: 'MATCHING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  riderId: number;
  driverId: number | null;
  startX: number;
  startY: number;
  destX: number;
  destY: number;
  createdAt: string;
  startTime: string | null;
  endTime: string | null;
  fare: number | null;
}

export interface TripRequest {
  start: Point;
  end: Point;
}

export async function requestTrip(data: TripRequest): Promise<Trip> {
  const response = await api.post<Trip>('/trips', data);
  return response.data;
}
