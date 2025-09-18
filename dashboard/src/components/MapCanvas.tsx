import React, { useEffect, useRef, useState } from 'react';
import BaseMap, { fitView, type Vec2 } from './map/BaseMap';
import TripLayer from './map/TripLayer';
import DriversLayer from './map/DriversLayer';
import { fetchMap } from '../services/mapService';
import type { MapData } from '../services/mapService';
import {fetchSnapshot, connectMonitoring, type MonitoringMessage} from '../services/monitoringService';
import type { TripDto, DriverLocation } from '../services/monitoringService';
import type { RouteResponse, RouteRequest } from '../services/routeService';
import { fetchRoute } from '../services/routeService';

export default function MapCanvas() {
  // Monitoring state
  const [drivers, setDrivers] = useState<Record<string, DriverLocation>>({});
  const [trips, setTrips] = useState<TripDto[]>([]);
  const [routes, setRoutes] = useState<Map<number, RouteResponse>>(new Map());
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [data, setData] = useState<MapData | null>(null);
  const [zoom, setZoom] = useState(1);
  const [offset, setOffset] = useState<Vec2>({ x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const lastPos = useRef<Vec2>({ x: 0, y: 0 });

  // Fetch map and fit
  useEffect(() => {
    fetchMap()
      .then((m) => {
        setData(m);
        const canvas = canvasRef.current;
        if (canvas) {
          const size = { x: canvas.clientWidth, y: canvas.clientHeight };
          const fit = fitView(m.nodes, size);
          setZoom(fit.zoom);
          setOffset(fit.offset);
        }
      })
      .catch(() => {});
  }, []);

  // Fetch monitoring snapshot & connect WS
  useEffect(() => {
    let ws: WebSocket | null = null;
    fetchSnapshot().then((snap) => {
      const initTrips = snap.trips || [];
      setTrips(initTrips);
      setDrivers(snap.drivers || {});
      // Fetch routes for all trips on initial snapshot
      fetchRoutesForTrips(initTrips);
    }).catch(() => {});
    ws = connectMonitoring(onMessage);
    return () => { if (ws) try { ws.close(); } catch {} };
  }, []);

  async function fetchRouteForTrip(trip: { id: number; startLongitude: number; startLatitude: number; endLongitude: number; endLatitude: number; }) {
    try {
      const req: RouteRequest = {
        startPoint: { x: trip.startLongitude, y: trip.startLatitude },
        destinationPoint: { x: trip.endLongitude, y: trip.endLatitude },
      };
      const r = await fetchRoute(req);
      setRoutes((prev) => {
        const m = new Map(prev);
        m.set(trip.id, r);
        return m;
      });
    } catch {}
  }

  function fetchRoutesForTrips(list: TripDto[]) {
    if (!list || !list.length) return;
    Promise.allSettled(list.map(t => fetchRouteForTrip(t))).then(() => {});
  }

  function onMessage(msg: MonitoringMessage) {
    switch (msg.type) {
      case 'driver.locations': {
        setDrivers(msg.drivers || {});
        break;
      }
      case 'trip.created': {
        const p = msg.payload;
        if (!p) break;
        const newTrip: TripDto = {
          id: p.tripId,
          status: 'MATCHING',
          riderId: (p.riderId ?? 0) as number,
          driverId: null,
          startLatitude: (p.startLatitude ?? 0) as number,
          startLongitude: (p.startLongitude ?? 0) as number,
          endLatitude: (p.endLatitude ?? 0) as number,
          endLongitude: (p.endLongitude ?? 0) as number,
        };
        setTrips((prev) => {
          if (prev.some(t => t.id === newTrip.id)) return prev;
          return [...prev, newTrip];
        });
        // Fetch route for the newly created trip
        fetchRouteForTrip({
          id: newTrip.id,
          startLongitude: newTrip.startLongitude,
          startLatitude: newTrip.startLatitude,
          endLongitude: newTrip.endLongitude,
          endLatitude: newTrip.endLatitude,
        });
        break;
      }
      case 'trip.matched': {
        const p = msg.payload;
        if (!p) break;
        setTrips((prev) =>
          prev.map((t) =>
            t.id === p.tripId
              ? { ...t, driverId: p.driverId ?? null, status: 'PICKING_UP' }
              : t
          )
        );
        break;
      }
      case 'trip.started': {
        const p = msg.payload;
        if (!p) break;
        setTrips((prev) =>
          prev.map((t) => {
            if (t.id !== p.tripId) return t;
            const next = { ...t, status: 'STARTED' as const };
            return next;
          })
        );
        break;
      }
      case 'trip.ended': {
        const p = msg.payload;
        if (!p) break;
        const endedId = p.tripId;
        setTrips((prev) => prev.filter((t) => t.id !== endedId));
        // Remove route for the ended trip
        setRoutes((prev) => {
          const m = new Map(prev);
          m.delete(endedId);
          return m;
        });
        break;
      }
    }
  }

  // Force background redraw when routes change (to clear removed routes from canvas)
  useEffect(() => {
    // trigger BaseMap useEffect by changing offset reference without changing values
    setOffset((o) => ({ x: o.x, y: o.y }));
  }, [routes]);

  // Interactions (zoom/pan)
  function onWheel(e: React.WheelEvent) {
    e.preventDefault();
    const canvas = canvasRef.current; if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
    const worldBefore = { x: mouse.x / zoom + offset.x, y: mouse.y / zoom + offset.y };
    const delta = -e.deltaY; const factor = Math.exp(delta * 0.001);
    const newZoom = Math.min(5, Math.max(0.2, zoom * factor));
    const newOffset = { x: worldBefore.x - mouse.x / newZoom, y: worldBefore.y - mouse.y / newZoom };
    setZoom(newZoom); setOffset(newOffset);
  }
  function onMouseDown(e: React.MouseEvent) { setDragging(true); lastPos.current = { x: e.clientX, y: e.clientY }; }
  function onMouseMove(e: React.MouseEvent) {
    if (!dragging) return;
    const dx = e.clientX - lastPos.current.x; const dy = e.clientY - lastPos.current.y; lastPos.current = { x: e.clientX, y: e.clientY };
    setOffset((o) => ({ x: o.x - dx / zoom, y: o.y - dy / zoom }));
  }
  function onMouseUp() { setDragging(false); }
  function onMouseLeave() { setDragging(false); }

  return (
    <BaseMap
      data={data}
      zoom={zoom}
      offset={offset}
      onWheel={onWheel}
      onMouseDown={onMouseDown}
      onMouseMove={onMouseMove}
      onMouseUp={onMouseUp}
      onMouseLeave={onMouseLeave}
      canvasRef={canvasRef}
    >
      <TripLayer canvasRef={canvasRef} trips={trips} routes={routes} zoom={zoom} offset={offset} />
      <DriversLayer drivers={drivers} trips={trips} zoom={zoom} offset={offset} dragging={dragging} />
      <div className="absolute top-3 left-3 bg-black/40 text-white text-xs px-2 py-1 rounded-full backdrop-blur border border-white/10">Map</div>
      <div className="absolute bottom-3 right-3 flex gap-2">
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => setZoom((z) => Math.min(5, z * 1.2))}>+</button>
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => setZoom((z) => Math.max(0.2, z / 1.2))}>-</button>
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => {
          if (!data || !canvasRef.current) return;
          const size = { x: canvasRef.current.clientWidth, y: canvasRef.current.clientHeight };
          const fit = fitView(data.nodes, size);
          setZoom(fit.zoom);
          setOffset(fit.offset);
        }}>Fit</button>
      </div>
    </BaseMap>
  );
}
