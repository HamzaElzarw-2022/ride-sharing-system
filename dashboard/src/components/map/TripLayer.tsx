import { useCallback, useEffect, useRef } from 'react';
import { User, Flag } from 'lucide-react';
import type { TripDto } from '../../services/monitoringService';
import type { RouteResponse, RouteStep } from '../../services/routeService';
import type { Vec2 } from './BaseMap';
import { worldToScreen } from './BaseMap';

// Configuration constants for trip visualization
const TRIP_PATH_OPACITY = 0.6; // Transparency level for trip paths (0-1)
const MAX_OFFSET_DISTANCE = 2; // Maximum offset distance in world units for parallel paths
const TRIP_PATH_WIDTH = 3; // Base width multiplier for trip paths
const TRIP_DASH_WIDTH = 1.5; // Base width multiplier for dashed lines (arcs and arrows)
const MIN_ARC_DISTANCE = 15; // Minimum distance to render dashed arcs (in world units)
const ICON_SIZE = 18; // Size of user and flag icons

// Generate distinct colors for trips using HSL color space
function getTripColor(tripId: number, alpha = TRIP_PATH_OPACITY): string {
  const hue = (tripId * 137.508) % 360; // Golden angle approximation for good distribution
  const saturation = 70 + (tripId % 3) * 10; // Vary saturation between 70-90%
  const lightness = 40 + (tripId % 2) * 10; // Vary lightness between 40-50%
  return `hsla(${hue}, ${saturation}%, ${lightness}%, ${alpha})`;
}

// Calculate perpendicular offset for path segments
function calculateOffset(from: Vec2, to: Vec2, offsetDistance: number): Vec2 {
  const dx = to.x - from.x;
  const dy = to.y - from.y;
  const length = Math.hypot(dx, dy) || 1;
  // Perpendicular vector (rotated 90 degrees)
  const perpX = -dy / length;
  const perpY = dx / length;
  return {
    x: perpX * offsetDistance,
    y: perpY * offsetDistance
  };
}

function drawDashedArc(ctx: CanvasRenderingContext2D, from: {x:number;y:number}, to: {x:number;y:number}, zoom: number, offset: Vec2, color: string, pathOffset = 0) {
  const offsetVec = calculateOffset(from, to, pathOffset);
  const offsetFrom = { x: from.x + offsetVec.x, y: from.y + offsetVec.y };
  const offsetTo = { x: to.x + offsetVec.x, y: to.y + offsetVec.y };
  
  const a = worldToScreen(offsetFrom, zoom, offset);
  const b = worldToScreen(offsetTo, zoom, offset);
  const mx = (a.x + b.x) / 2; const my = (a.y + b.y) / 2;
  const dx = b.x - a.x; const dy = b.y - a.y; const len = Math.hypot(dx, dy) || 1;
  const nx = -dy / len; const ny = dx / len; const k = 10 * zoom;
  ctx.save();
  ctx.setLineDash([6 * zoom, 4 * zoom]);
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(1, TRIP_DASH_WIDTH * zoom);
  ctx.beginPath();
  ctx.moveTo(a.x, a.y);
  ctx.quadraticCurveTo(mx + nx * k, my + ny * k, b.x, b.y);
  ctx.stroke();
  ctx.restore();
}

function drawPolyline(ctx: CanvasRenderingContext2D, points: {x:number;y:number}[], zoom: number, offset: Vec2, color: string, pathOffset = 0) {
  if (points.length < 2) return;
  
  // Calculate offset points
  const offsetPoints = [];
  for (let i = 0; i < points.length; i++) {
    if (i === 0) {
      // First point: use direction to next point
      const offsetVec = calculateOffset(points[i], points[i + 1], pathOffset);
      offsetPoints.push({ x: points[i].x + offsetVec.x, y: points[i].y + offsetVec.y });
    } else if (i === points.length - 1) {
      // Last point: use direction from previous point
      const offsetVec = calculateOffset(points[i - 1], points[i], pathOffset);
      offsetPoints.push({ x: points[i].x + offsetVec.x, y: points[i].y + offsetVec.y });
    } else {
      // Middle points: average offset from both adjacent segments
      const offsetVec1 = calculateOffset(points[i - 1], points[i], pathOffset);
      const offsetVec2 = calculateOffset(points[i], points[i + 1], pathOffset);
      const avgOffset = {
        x: (offsetVec1.x + offsetVec2.x) / 2,
        y: (offsetVec1.y + offsetVec2.y) / 2
      };
      offsetPoints.push({ x: points[i].x + avgOffset.x, y: points[i].y + avgOffset.y });
    }
  }
  
  ctx.save();
  ctx.setLineDash([]);
  ctx.lineCap = 'round';
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(2, TRIP_PATH_WIDTH * zoom);
  ctx.beginPath();
  const first = worldToScreen(offsetPoints[0], zoom, offset);
  ctx.moveTo(first.x, first.y);
  for (let i = 1; i < offsetPoints.length; i++) {
    const sp = worldToScreen(offsetPoints[i], zoom, offset);
    ctx.lineTo(sp.x, sp.y);
  }
  ctx.stroke();
  ctx.restore();
}

function drawDashedLine(ctx: CanvasRenderingContext2D, from: {x:number;y:number}, to: {x:number;y:number}, zoom: number, offset: Vec2, color: string, pathOffset = 0) {
  const offsetVec = calculateOffset(from, to, pathOffset);
  const offsetFrom = { x: from.x + offsetVec.x, y: from.y + offsetVec.y };
  const offsetTo = { x: to.x + offsetVec.x, y: to.y + offsetVec.y };
  
  const a = worldToScreen(offsetFrom, zoom, offset);
  const b = worldToScreen(offsetTo, zoom, offset);
  ctx.save();
  ctx.setLineDash([6 * zoom, 4 * zoom]);
  ctx.strokeStyle = color;
  ctx.lineWidth = Math.max(1, TRIP_DASH_WIDTH * zoom);
  ctx.beginPath();
  ctx.moveTo(a.x, a.y);
  ctx.lineTo(b.x, b.y);
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

export default function TripLayer({ trips, routes, zoom = 13, offset = { x: 0, y: 0 } }: {
  trips: TripDto[];
  routes: Map<number, RouteResponse>;
  zoom?: number;
  offset?: Vec2;
}) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const tripOffsetsRef = useRef<Map<number, number>>(new Map());
  const view = useRef({ zoom, offset });
  view.current = { zoom, offset };

  const render = useCallback(() => {
    const canvas = canvasRef.current; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;

    // Clear the entire canvas before drawing
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Get active trips and manage their offsets for stable rendering
    const activeTrips = trips.filter(t => t.status === 'PICKING_UP' || t.status === 'STARTED');
    const tripOffsets = tripOffsetsRef.current;
    const activeTripIds = new Set(activeTrips.map(t => t.id));

    // Clean up offsets for trips that are no longer active
    for (const tripId of tripOffsets.keys()) {
      if (!activeTripIds.has(tripId)) {
        tripOffsets.delete(tripId);
      }
    }

    // Assign stable offsets to active trips
    const usedOffsets = new Set(tripOffsets.values());
    activeTrips.forEach(t => {
      if (!tripOffsets.has(t.id)) {
        let i = 0;
        while (true) {
          if (i === 0 && !usedOffsets.has(0)) {
            tripOffsets.set(t.id, 0);
            usedOffsets.add(0);
            break;
          }
          const offsetVal = MAX_OFFSET_DISTANCE * Math.ceil(i / 2) * (i % 2 === 1 ? 1 : -1);
          if (!usedOffsets.has(offsetVal)) {
            tripOffsets.set(t.id, offsetVal);
            usedOffsets.add(offsetVal);
            break;
          }
          i++;
        }
      }
    });
    
    // Draw routes and connecting arcs with unique colors and stable offsets
    activeTrips.forEach((t) => {
      const r = routes.get(t.id);
      if (!r) return;
      
      // Generate unique color with transparency
      const color = getTripColor(t.id);
      
      // Get stable offset distance
      const offsetDistance = tripOffsets.get(t.id) ?? 0;
      
      // Check if start arc should be rendered based on distance (only for PICKING_UP trips)
      const startDistance = Math.hypot(
        r.startPointProjection.originalPoint.x - r.startPointProjection.projectionPoint.x,
        r.startPointProjection.originalPoint.y - r.startPointProjection.projectionPoint.y
      );
      if (t.status === 'PICKING_UP' && startDistance >= MIN_ARC_DISTANCE) {
        // start original -> start projection (dashed arc) - only for PICKING_UP
        drawDashedArc(ctx, r.startPointProjection.originalPoint, r.startPointProjection.projectionPoint, view.current.zoom, view.current.offset, color, offsetDistance);
      }
      
      // polyline along the route from start projection through steps to destination projection
      const pts = buildRoutePolyline(r);
      drawPolyline(ctx, pts, view.current.zoom, view.current.offset, color, offsetDistance);
      
      // Check if destination arrow should be rendered based on distance
      const destDistance = Math.hypot(
        r.destinationPointProjection.projectionPoint.x - r.destinationPointProjection.originalPoint.x,
        r.destinationPointProjection.projectionPoint.y - r.destinationPointProjection.originalPoint.y
      );
      if (destDistance >= MIN_ARC_DISTANCE) {
        // destination projection -> destination original (dashed line)
        drawDashedLine(ctx, r.destinationPointProjection.projectionPoint, r.destinationPointProjection.originalPoint, view.current.zoom, view.current.offset, color, offsetDistance);
      }
    });
  }, [trips, routes]);

  useEffect(() => {
    const handleRedraw = (e: Event) => {
      const detail = (e as CustomEvent).detail;
      view.current = detail;
      render();
    };
    window.addEventListener('map-redraw', handleRedraw);
    return () => window.removeEventListener('map-redraw', handleRedraw);
  }, [render]);

  useEffect(() => { render(); }, [render, zoom, offset]);
  useEffect(() => {
    function onBaseMapRendered() { render(); }
    window.addEventListener('basemap-rendered', onBaseMapRendered);
    return () => window.removeEventListener('basemap-rendered', onBaseMapRendered);
  }, [render]);

  useEffect(() => {
    function resize() {
      const canvas = canvasRef.current; const container = containerRef.current; if (!canvas || !container) return;
      const dpr = window.devicePixelRatio || 1; const rect = container.getBoundingClientRect();
      canvas.width = Math.floor(rect.width * dpr); canvas.height = Math.floor(rect.height * dpr);
      canvas.style.width = `${rect.width}px`; canvas.style.height = `${rect.height}px`;
      const ctx = canvas.getContext('2d'); if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      render();
    }
    const ro = new ResizeObserver(resize);
    if (containerRef.current) ro.observe(containerRef.current);
    resize();
    return () => ro.disconnect();
  }, [render]);

  return (
    <div ref={containerRef} className="w-full h-full absolute top-0 left-0 pointer-events-none">
      <canvas
        ref={canvasRef}
        className="w-full h-full"
      />
      {/* Matching trips: only user icon at start point */}
      {trips.filter(t => t.status === 'MATCHING').map(t => {
        const sp = worldToScreen({ x: t.startX, y: t.startY }, zoom, offset);
        return (
          <div key={`match-${t.id}`} className="absolute" style={{ left: sp.x - 8, top: sp.y - 8 }}>
            <User size={16} className="text-yellow-300 drop-shadow" />
          </div>
        );
      })}
      {/* Accepted trips (PICKING_UP): render user icon at start original and dashed arc */}
      {trips.filter(t => t.status === 'PICKING_UP').map(t => {
        const r = routes.get(t.id);
        if (!r) return null;
        const startOriginal = worldToScreen(r.startPointProjection.originalPoint, zoom, offset);
        return (
          <div key={`pickup-${t.id}`} className="absolute" style={{ left: startOriginal.x - 9, top: startOriginal.y - 9 }}>
            <User size={ICON_SIZE} className="text-yellow-300 drop-shadow" />
          </div>
        );
      })}
      {/* Started trips (STARTED): render flag icon at destination original only */}
      {trips.filter(t => t.status === 'STARTED').map(t => {
        const r = routes.get(t.id);
        if (!r) return null;
        const destOriginal = worldToScreen(r.destinationPointProjection.originalPoint, zoom, offset);
        return (
          <div key={`started-${t.id}`} className="absolute" style={{ left: destOriginal.x - 9, top: destOriginal.y - 9 }}>
            <Flag size={ICON_SIZE} className="text-red-400 drop-shadow" />
          </div>
        );
      })}
    </div>
  );
}
