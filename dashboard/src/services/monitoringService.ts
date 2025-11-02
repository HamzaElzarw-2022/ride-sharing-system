import api from './api';
import { API_HOST } from './api';

export type TripStatus = 'MATCHING' | 'PICKING_UP' | 'STARTED' | 'COMPLETED' | 'CANCELLED' | 'NO_DRIVERS_MATCHED';

export interface TripDto {
  id: number;
  status: TripStatus;
  riderId: number;
  driverId?: number | null;
  startX: number;
  startY: number;
  destX: number;
  destY: number;
  // other fields omitted
}

export interface DriverLocation {
  x: number; // internal map X
  y: number; // internal map Y
  degree: number; // orientation in degrees
}

export interface MonitoringSnapshot {
  trips: TripDto[];
  drivers: Record<string, DriverLocation>;
}

export async function fetchSnapshot(): Promise<MonitoringSnapshot> {
  const { data } = await api.get<MonitoringSnapshot>('/monitoring/snapshot');
  return data;
}

export type MonitoringMessage =
  | { type: 'driver.locations'; ts: number; drivers: Record<string, DriverLocation> }
  | { type: 'trip.created' | 'trip.matched' | 'trip.started' | 'trip.ended'; ts: number;
      payload: {
        tripId: number;
        // if type is trip.created
        riderId?: number | null;
        startY?: number | null;
        startX?: number | null;
        destY?: number | null;
        destX?: number | null;
        createdAt?: Date | null;
        // if type is trip.matched
        driverId?: number | null;
        // if type is trip.started
        startTime?: Date | null;
        // if type is trip.ended
        endTime?: Date | null;
      }
    };

export function connectMonitoring(onMessage: (msg: MonitoringMessage) => void): WebSocket {
  const url = new URL(`wss://${API_HOST}/ws/monitoring`);
  const ws = new WebSocket(url);
  ws.onmessage = (ev) => {
    try {
      const msg = JSON.parse(ev.data);
      onMessage(msg as MonitoringMessage);
    } catch (e) {
      // ignore malformed
    }
  };
  return ws;
}
