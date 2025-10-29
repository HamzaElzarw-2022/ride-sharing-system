import { getToken } from './authService';

export type TripMatchedPayload = {
  tripId: number;
  driverId: number;
};

export type TripStartedPayload = {
  tripId: number;
  startTime: string;
};

export type TripEndedPayload = {
  tripId: number;
  endTime: string;
};

export type DriverLocationPayload = {
  tripId: number;
  driverId: number;
  x: number;
  y: number;
  degree: number;
};

export type TripMessage =
  | { type: 'trip.matched'; ts: number; payload: TripMatchedPayload }
  | { type: 'trip.started'; ts: number; payload: TripStartedPayload }
  | { type: 'trip.ended'; ts: number; payload: TripEndedPayload }
  | { type: 'driver.location'; ts: number; payload: DriverLocationPayload };

export function connectTrip(tripId: number, onMessage: (msg: TripMessage) => void): WebSocket {
  const token = getToken();
  const wsUrl = `ws://localhost:8080/ws/trip/${tripId}${token ? `?token=${token}` : ''}`;
  console.log(`[TripWS] attempting to connect for trip ${tripId}`);
  const ws = new WebSocket(wsUrl);

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data) as TripMessage;
      onMessage(msg);
    } catch (error) {
      console.error('Failed to parse trip message', error);
    }
  };

  ws.onopen = () => {
    console.log(`[TripWS] connected for trip ${tripId}`);
  };

  ws.onclose = (event) => {
    console.log(`[TripWS] disconnected for trip ${tripId}`, event.reason);
  };

  ws.onerror = (error) => {
    console.error(`[TripWS] error for trip ${tripId}`, error);
  };

  return ws;
}
