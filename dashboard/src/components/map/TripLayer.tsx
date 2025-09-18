import React, { useEffect, useRef } from 'react';
import { User, Flag } from 'lucide-react';
import type { TripDto } from '../../services/monitoringService';
import type { RouteResponse, RouteStep } from '../../services/routeService';
import type { Vec2 } from './BaseMap';
import { worldToScreen } from './BaseMap';

function drawDashedArc(ctx: CanvasRenderingContext2D, from: {x:number;y:number}, to: {x:number;y:number}, zoom: number, offset: Vec2, color: string) {
  const a = worldToScreen(from, zoom, offset);
  const b = worldToScreen(to, zoom, offset);
  const mx = (a.x + b.x) / 2; const my = (a.y + b.y) / 2;
  const dx = b.x - a.x; const dy = b.y - a.y; const len = Math.hypot(dx, dy) || 1;
  const nx = -dy / len; const ny = dx / len; const k = 10 * zoom;
  ctx.save();
  ctx.setLineDash([6 * zoom, 4 * zoom]);
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(1, 1.5 * zoom);
  ctx.beginPath();
  ctx.moveTo(a.x, a.y);
  ctx.quadraticCurveTo(mx + nx * k, my + ny * k, b.x, b.y);
  ctx.stroke();
  ctx.restore();
}

function drawPolyline(ctx: CanvasRenderingContext2D, points: {x:number;y:number}[], zoom: number, offset: Vec2, color: string) {
  if (points.length < 2) return;
  ctx.save();
  ctx.setLineDash([]);
  ctx.lineCap = 'round';
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(2, 3 * zoom);
  ctx.beginPath();
  const first = worldToScreen(points[0], zoom, offset);
  ctx.moveTo(first.x, first.y);
  for (let i = 1; i < points.length; i++) {
    const sp = worldToScreen(points[i], zoom, offset);
    ctx.lineTo(sp.x, sp.y);
  }
  ctx.stroke();
  ctx.restore();
}

function drawDashedArrow(ctx: CanvasRenderingContext2D, from: {x:number;y:number}, to: {x:number;y:number}, zoom: number, offset: Vec2, color: string) {
  const a = worldToScreen(from, zoom, offset);
  const b = worldToScreen(to, zoom, offset);
  ctx.save();
  ctx.setLineDash([6 * zoom, 4 * zoom]);
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(1, 1.5 * zoom);
  ctx.beginPath();
  ctx.moveTo(a.x, a.y);
  ctx.lineTo(b.x, b.y);
  ctx.stroke();
  // arrow head
  const dx = b.x - a.x; const dy = b.y - a.y; const len = Math.hypot(dx, dy) || 1;
  const ux = dx / len; const uy = dy / len;
  const size = 8 * zoom;
  ctx.beginPath();
  ctx.moveTo(b.x, b.y);
  ctx.lineTo(b.x - ux * size + -uy * (size * 0.5), b.y - uy * size + ux * (size * 0.5));
  ctx.moveTo(b.x, b.y);
  ctx.lineTo(b.x - ux * size + uy * (size * 0.5), b.y - uy * size + -ux * (size * 0.5));
  ctx.stroke();
  ctx.restore();
}

function buildRoutePolyline(route: RouteResponse): {x:number;y:number}[] {
  const pts: {x:number;y:number}[] = [];
  // start projection point
  pts.push(route.startPointProjection.projectionPoint);
  // all route steps
  for (const step of route.route as RouteStep[]) {
    pts.push({ x: step.x, y: step.y });
  }
  // destination projection point
  pts.push(route.destinationPointProjection.projectionPoint);
  return pts;
}

export default function TripLayer({ canvasRef, trips, routes, zoom, offset }: {
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
  trips: TripDto[];
  routes: Map<number, RouteResponse>;
  zoom: number;
  offset: Vec2;
}) {
  const raf = useRef<number | null>(null);

  function render() {
    const canvas = canvasRef.current; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;

    // Draw routes and connecting arcs
    trips.forEach((t) => {
      const r = routes.get(t.id);
      if (!r) return;
      if (t.status !== 'PICKING_UP' && t.status !== 'STARTED') return;
      const color = t.status === 'STARTED' ? '#10b981' : '#3b82f6'; // green for started, blue for accepted
      // start original -> start projection (dashed arc)
      drawDashedArc(ctx, r.startPointProjection.originalPoint, r.startPointProjection.projectionPoint, zoom, offset, color);
      // polyline along the route from start projection through steps to destination projection
      const pts = buildRoutePolyline(r);
      drawPolyline(ctx, pts, zoom, offset, color);
      // destination projection -> destination original (dashed arrow)
      drawDashedArrow(ctx, r.destinationPointProjection.projectionPoint, r.destinationPointProjection.originalPoint, zoom, offset, color);
    });
  }

  useEffect(() => { render(); }, [trips, routes, zoom, offset]);
  useEffect(() => {
    function onBaseMapRendered() { render(); }
    window.addEventListener('basemap-rendered', onBaseMapRendered);
    return () => window.removeEventListener('basemap-rendered', onBaseMapRendered);
  }, [trips, routes, zoom, offset]);
  useEffect(() => () => { if (raf.current) cancelAnimationFrame(raf.current); }, []);

  return (
    <>
      {/* Matching trips: only user icon at start point */}
      {trips.filter(t => t.status === 'MATCHING').map(t => {
        const sp = worldToScreen({ x: t.startLongitude, y: t.startLatitude }, zoom, offset);
        return (
          <div key={`match-${t.id}`} className="absolute" style={{ left: sp.x - 8, top: sp.y - 8 }}>
            <User size={16} className="text-yellow-300 drop-shadow" />
          </div>
        );
      })}
      {/* Accepted and Started: render icons at start original (user) and destination original (flag) */}
      {trips.filter(t => t.status === 'PICKING_UP' || t.status === 'STARTED').map(t => {
        const r = routes.get(t.id);
        if (!r) return null;
        const startOriginal = worldToScreen(r.startPointProjection.originalPoint, zoom, offset);
        const destOriginal = worldToScreen(r.destinationPointProjection.originalPoint, zoom, offset);
        return (
          <React.Fragment key={`rt-${t.id}`}>
            <div className="absolute" style={{ left: startOriginal.x - 8, top: startOriginal.y - 8 }}>
              <User size={16} className="text-yellow-300 drop-shadow" />
            </div>
            <div className="absolute" style={{ left: destOriginal.x - 7, top: destOriginal.y - 10 }}>
              <Flag size={14} className="text-red-400 drop-shadow" />
            </div>
          </React.Fragment>
        );
      })}
    </>
  );
}
