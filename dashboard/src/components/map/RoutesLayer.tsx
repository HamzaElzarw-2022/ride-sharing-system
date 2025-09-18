import React, { useEffect, useRef } from 'react';
import type { TripDto } from '../../services/monitoringService';
import { fetchRoute, type RouteResponse } from '../../services/routeService';
import type { Vec2 } from './BaseMap';
import { worldToScreen } from './BaseMap';

function drawRoute(ctx: CanvasRenderingContext2D, route: RouteResponse, zoom: number, offset: Vec2) {
  if (!route.route || route.route.length === 0) return;
  ctx.save();
  ctx.lineCap = 'round';
  ctx.strokeStyle = '#22d3ee';
  ctx.lineWidth = Math.max(2, 3 * zoom);
  ctx.beginPath();
  const first = route.route[0];
  const s0 = worldToScreen({ x: first.x, y: first.y }, zoom, offset);
  ctx.moveTo(s0.x, s0.y);
  for (let i = 1; i < route.route.length; i++) {
    const p = route.route[i];
    const sp = worldToScreen({ x: p.x, y: p.y }, zoom, offset);
    ctx.lineTo(sp.x, sp.y);
  }
  ctx.stroke();
  const pairs: { from: {x:number;y:number}; to: {x:number;y:number} }[] = [];
  if ((route as any).startPointProjection) {
    pairs.push({ from: (route as any).startPointProjection.originalPoint, to: (route as any).startPointProjection.projectionPoint });
  }
  if ((route as any).destinationPointProjection) {
    pairs.push({ from: (route as any).destinationPointProjection.originalPoint, to: (route as any).destinationPointProjection.projectionPoint });
  }
  ctx.setLineDash([6 * zoom, 4 * zoom]);
  ctx.strokeStyle = '#60a5fa';
  ctx.lineWidth = Math.max(1, 1.5 * zoom);
  for (const pr of pairs) {
    const a = worldToScreen(pr.from, zoom, offset);
    const b = worldToScreen(pr.to, zoom, offset);
    const mx = (a.x + b.x) / 2; const my = (a.y + b.y) / 2;
    const dx = b.x - a.x; const dy = b.y - a.y; const len = Math.hypot(dx, dy) || 1;
    const nx = -dy / len; const ny = dx / len; const k = 10 * zoom;
    ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.quadraticCurveTo(mx + nx * k, my + ny * k, b.x, b.y); ctx.stroke();
  }
  ctx.restore();
}

export default function RoutesLayer({ canvasRef, trips, drivers, zoom, offset, routeCache }: {
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
  trips: TripDto[];
  drivers: Record<string, { x:number;y:number;degree:number }>;
  zoom: number;
  offset: Vec2;
  routeCache: React.MutableRefObject<Map<number, RouteResponse>>;
}) {
  const raf = useRef<number | null>(null);

  function render() {
    const canvas = canvasRef.current; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    routeCache.current.forEach((route, id) => {
      const active = trips.some(t => t.id === id && (t.status === 'MATCHING' || t.status === 'PICKING_UP' || t.status === 'STARTED'));
      if (active) drawRoute(ctx, route, zoom, offset);
    });
  }

  // fetch/purge routes based on trips & drivers
  useEffect(() => {
    const activeIds = new Set(trips.filter(t => t.status === 'MATCHING' || t.status === 'PICKING_UP' || t.status === 'STARTED').map(t => t.id));
    Array.from(routeCache.current.keys()).forEach((id) => { if (!activeIds.has(id)) routeCache.current.delete(id); });
    const need: Array<Promise<void>> = [];
    trips.forEach((t) => {
      const id = t.id; if (routeCache.current.has(id)) return;
      if (t.status === 'PICKING_UP') {
        const drv = t.driverId != null ? drivers[String(t.driverId)] : undefined; if (!drv) return;
        const req = { startPoint: { x: drv.x, y: drv.y }, destinationPoint: { x: t.startLongitude, y: t.startLatitude } };
        need.push(fetchRoute(req).then((r) => { routeCache.current.set(id, r); render(); }).catch(() => {}));
      } else if (t.status === 'STARTED') {
        const req = { startPoint: { x: t.startLongitude, y: t.startLatitude }, destinationPoint: { x: t.endLongitude, y: t.endLatitude } };
        need.push(fetchRoute(req).then((r) => { routeCache.current.set(id, r); render(); }).catch(() => {}));
      }
    });
    if (need.length) Promise.allSettled(need).then(() => render());
  }, [trips, drivers]);

  // re-render on zoom/offset changes
  useEffect(() => { render(); }, [zoom, offset, trips]);

  // cleanup raf if any (not used now but safe)
  useEffect(() => () => { if (raf.current) cancelAnimationFrame(raf.current); }, []);

  return null;
}
