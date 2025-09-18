import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { connectMonitoring, fetchSnapshot, type MonitoringMessage, type TripDto, type DriverLocation } from '../services/monitoringService';
import { fetchRoute, type RouteRequest, type RouteResponse } from '../services/routeService';

export type SidebarTab = 'events' | 'trips' | 'tripDetails';

export type EventItem = {
  id: string;
  type: 'trip.created' | 'trip.matched' | 'trip.started' | 'trip.ended';
  ts: number;
  payload: any;
};

export type MonitoringContextType = {
  trips: TripDto[];
  drivers: Record<string, DriverLocation>;
  routes: Map<number, RouteResponse>;
  events: EventItem[];
  selectedTripId: number | null;
  setSelectedTripId: (id: number | null) => void;
};

const MonitoringContext = createContext<MonitoringContextType | undefined>(undefined);

export function MonitoringProvider({ children }: { children: React.ReactNode }) {
  const [drivers, setDrivers] = useState<Record<string, DriverLocation>>({});
  const [trips, setTrips] = useState<TripDto[]>([]);
  const [routes, setRoutes] = useState<Map<number, RouteResponse>>(new Map());
  const [events, setEvents] = useState<EventItem[]>([]);
  const [selectedTripId, setSelectedTripId] = useState<number | null>(null);

  // Initial snapshot + websocket
  useEffect(() => {
    let ws: WebSocket | null = null;
    fetchSnapshot()
      .then((snap) => {
        const initTrips = snap.trips || [];
        setTrips(initTrips);
        setDrivers(snap.drivers || {});
        // Fetch routes for all trips on initial snapshot
        fetchRoutesForTrips(initTrips);
      })
      .catch(() => {});

    ws = connectMonitoring(onMessage);
    return () => { if (ws) try { ws.close(); } catch {} };
  }, []);

  function onMessage(msg: MonitoringMessage) {
    switch (msg.type) {
      case 'driver.locations': {
        setDrivers(msg.drivers || {});
        break;
      }
      case 'trip.created': {
        const p = msg.payload; if (!p) break;
        const newTrip: TripDto = {
          id: p.tripId,
          status: 'MATCHING',
          riderId: (p.riderId ?? 0) as number,
          driverId: null,
          startY: (p.startY ?? 0) as number,
          startX: (p.startX ?? 0) as number,
          destY: (p.destY ?? 0) as number,
          destX: (p.destX ?? 0) as number,
        };
        setTrips((prev) => (prev.some(t => t.id === newTrip.id) ? prev : [...prev, newTrip]));
        fetchRouteForTrip({ id: newTrip.id, startLongitude: newTrip.startX, startLatitude: newTrip.startY, endLongitude: newTrip.destX, endLatitude: newTrip.destY });
        pushEvent(msg);
        break;
      }
      case 'trip.matched': {
        const p = msg.payload; if (!p) break;
        setTrips((prev) => prev.map((t) => (t.id === p.tripId ? { ...t, driverId: p.driverId ?? null, status: 'PICKING_UP' } : t)));
        pushEvent(msg);
        break;
      }
      case 'trip.started': {
        const p = msg.payload; if (!p) break;
        setTrips((prev) => prev.map((t) => (t.id === p.tripId ? { ...t, status: 'STARTED' } : t)));
        pushEvent(msg);
        break;
      }
      case 'trip.ended': {
        const p = msg.payload; if (!p) break;
        const endedId = p.tripId;
        setTrips((prev) => prev.filter((t) => t.id !== endedId));
        setRoutes((prev) => { const m = new Map(prev); m.delete(endedId); return m; });
        if (selectedTripId === endedId) setSelectedTripId(null);
        pushEvent(msg);
        break;
      }
    }
  }

  function pushEvent(msg: MonitoringMessage) {
    if (!msg.type.startsWith('trip.')) return;
    const id = `${msg.type}:${(msg as any).payload?.tripId ?? msg.ts}`;
    const item: EventItem = { id, type: msg.type as EventItem['type'], ts: msg.ts, payload: (msg as any).payload };
    setEvents((prev) => {
      const next = [item, ...prev];
      if (next.length > 15) next.length = 15;
      return next;
    });
  }

  async function fetchRouteForTrip(trip: { id: number; startLongitude: number; startLatitude: number; endLongitude: number; endLatitude: number; }) {
    try {
      const req: RouteRequest = {
        startPoint: { x: trip.startLongitude, y: trip.startLatitude },
        destinationPoint: { x: trip.endLongitude, y: trip.endLatitude },
      };
      const r = await fetchRoute(req);
      setRoutes((prev) => { const m = new Map(prev); m.set(trip.id, r); return m; });
    } catch {}
  }

  function fetchRoutesForTrips(list: TripDto[]) {
    if (!list || !list.length) return;
    Promise.allSettled(list.map(t => fetchRouteForTrip({ id: t.id, startLongitude: t.startX, startLatitude: t.startY, endLongitude: t.destX, endLatitude: t.destY }))).then(() => {});
  }

  const value = useMemo<MonitoringContextType>(() => ({ trips, drivers, routes, events, selectedTripId, setSelectedTripId }), [trips, drivers, routes, events, selectedTripId]);

  return (
    <MonitoringContext.Provider value={value}>
      {children}
    </MonitoringContext.Provider>
  );
}

export function useMonitoring() {
  const ctx = useContext(MonitoringContext);
  if (!ctx) throw new Error('useMonitoring must be used within MonitoringProvider');
  return ctx;
}
