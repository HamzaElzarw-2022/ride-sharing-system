import React, { useEffect, useRef, useState } from 'react';
import { fetchMap } from '../services/mapService';
import type { MapData, MapNode } from '../services/mapService';
import { fetchSnapshot, connectMonitoring } from '../services/monitoringService';
import type { TripDto, DriverLocation } from '../services/monitoringService';
import { fetchRoute } from '../services/routeService';
import type { RouteResponse } from '../services/routeService';
import { Car, User } from 'lucide-react';

type Vec2 = { x: number; y: number };

const BG = '#0b1220'; // dark asphalt
const GRID = '#101826'; // subtle grid
const ROAD_CASING = '#0a0f1a'; // darker outline
const ROAD_FILL = '#1e293b'; // asphalt fill
const ROAD_CENTER = '#facc15'; // yellow center
const ROAD_CENTER_ALT = '#94a3b8'; // gray center for slower roads
const INTERSECTION_HALO = 'rgba(255,255,255,0.06)';

function drawGrid(ctx: CanvasRenderingContext2D, width: number, height: number, zoom: number, offset: Vec2) {
  const step = 50 * zoom;
  ctx.save();
  ctx.strokeStyle = GRID;
  ctx.lineWidth = 1;

  const startX = -((offset.x * zoom) % step);
  const startY = -((offset.y * zoom) % step);
  for (let x = startX; x < width; x += step) {
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, height);
    ctx.stroke();
  }
  for (let y = startY; y < height; y += step) {
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(width, y);
    ctx.stroke();
  }
  ctx.restore();
}

function fitView(nodes: MapNode[], canvasSize: Vec2): { zoom: number; offset: Vec2 } {
  if (!nodes.length) return { zoom: 1, offset: { x: 0, y: 0 } };
  const minX = Math.min(...nodes.map(n => n.x));
  const minY = Math.min(...nodes.map(n => n.y));
  const maxX = Math.max(...nodes.map(n => n.x));
  const maxY = Math.max(...nodes.map(n => n.y));
  const padding = 40;
  const width = maxX - minX || 1;
  const height = maxY - minY || 1;
  const zoomX = (canvasSize.x - padding * 2) / width;
  const zoomY = (canvasSize.y - padding * 2) / height;
  const zoom = Math.max(0.1, Math.min(zoomX, zoomY));
  const offset: Vec2 = { x: minX - padding / zoom, y: minY - padding / zoom };
  return { zoom, offset };
}

function worldToScreen(p: Vec2, zoom: number, offset: Vec2): Vec2 {
  return { x: (p.x - offset.x) * zoom, y: (p.y - offset.y) * zoom };
}

function drawRoute(ctx: CanvasRenderingContext2D, route: RouteResponse, zoom: number, offset: Vec2) {
  // draw route polyline
  if (!route.route || route.route.length === 0) return;
  ctx.save();
  ctx.lineCap = 'round';
  ctx.strokeStyle = '#22d3ee'; // cyan
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
  // dashed arcs between original and projections at endpoints
  const pairs: { from: Vec2; to: Vec2 }[] = [];
  if (route.startPointProjection) {
    pairs.push({ from: route.startPointProjection.originalPoint, to: route.startPointProjection.projectionPoint });
  }
  if (route.destinationPointProjection) {
    pairs.push({ from: route.destinationPointProjection.originalPoint, to: route.destinationPointProjection.projectionPoint });
  }
  ctx.setLineDash([6 * zoom, 4 * zoom]);
  ctx.strokeStyle = '#60a5fa'; // light blue
  ctx.lineWidth = Math.max(1, 1.5 * zoom);
  for (const pr of pairs) {
    const a = worldToScreen(pr.from, zoom, offset);
    const b = worldToScreen(pr.to, zoom, offset);
    // quadratic arc control point at mid with slight offset
    const mx = (a.x + b.x) / 2; const my = (a.y + b.y) / 2;
    const dx = b.x - a.x; const dy = b.y - a.y;
    const len = Math.hypot(dx, dy) || 1;
    const nx = -dy / len; const ny = dx / len;
    const k = 10 * zoom; // arc height
    ctx.beginPath();
    ctx.moveTo(a.x, a.y);
    ctx.quadraticCurveTo(mx + nx * k, my + ny * k, b.x, b.y);
    ctx.stroke();
  }
  ctx.restore();
}

function drawMap(ctx: CanvasRenderingContext2D, data: MapData, size: Vec2, zoom: number, offset: Vec2) {
  // background
  ctx.fillStyle = BG;
  ctx.fillRect(0, 0, size.x, size.y);

  drawGrid(ctx, size.x, size.y, zoom, offset);

  // subtle vignette for depth
  const grad = ctx.createRadialGradient(size.x/2, size.y/2, Math.min(size.x,size.y)*0.2, size.x/2, size.y/2, Math.max(size.x,size.y)*0.7);
  grad.addColorStop(0, 'rgba(0,0,0,0)');
  grad.addColorStop(1, 'rgba(0,0,0,0.25)');
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, size.x, size.y);

  const nodeById: Record<number, MapNode> = Object.fromEntries(data.nodes.map(n => [n.id, n]));

  // roads (edges) with casing + fill + center line
  ctx.lineCap = 'round';
  for (const e of data.edges) {
    const a = nodeById[e.startId];
    const b = nodeById[e.endId];
    if (!a || !b) continue;
    const A = worldToScreen(a, zoom, offset);
    const B = worldToScreen(b, zoom, offset);

    const baseWidth = 6; // base road width in screen px at zoom=1
    const speedFactor = Math.min(1.6, Math.max(0.8, e.speed / 50));
    const roadWidth = Math.max(3, baseWidth * speedFactor * zoom);
    const casingWidth = roadWidth + Math.max(2, 2.5 * zoom);

    // Casing (outer dark outline)
    ctx.strokeStyle = ROAD_CASING;
    ctx.lineWidth = casingWidth;
    ctx.beginPath();
    ctx.moveTo(A.x, A.y);
    ctx.lineTo(B.x, B.y);
    ctx.stroke();

    // Asphalt fill
    ctx.strokeStyle = ROAD_FILL;
    ctx.lineWidth = roadWidth;
    ctx.beginPath();
    ctx.moveTo(A.x, A.y);
    ctx.lineTo(B.x, B.y);
    ctx.stroke();

    // Center dashed line
    const centerColor = e.speed >= 70 ? ROAD_CENTER : ROAD_CENTER_ALT;
    const dash = Math.max(6, 10 * zoom);
    const gap = dash * 0.8;
    ctx.save();
    ctx.strokeStyle = centerColor;
    ctx.lineWidth = Math.max(1, 1.5 * zoom);
    ctx.setLineDash([dash, gap]);
    ctx.beginPath();
    ctx.moveTo(A.x, A.y);
    ctx.lineTo(B.x, B.y);
    ctx.stroke();
    ctx.restore();
  }

  // intersections (nodes): subtle junction discs
  for (const n of data.nodes) {
    const P = worldToScreen(n, zoom, offset);
    const r = Math.max(3, 5 * zoom);
    // halo merges with casing
    ctx.fillStyle = INTERSECTION_HALO;
    ctx.beginPath();
    ctx.arc(P.x, P.y, r * 1.4, 0, Math.PI * 2);
    ctx.fill();
    // core highlight
    ctx.fillStyle = ROAD_FILL;
    ctx.beginPath();
    ctx.arc(P.x, P.y, r, 0, Math.PI * 2);
    ctx.fill();
  }
}

export default function MapCanvas() {
  // Monitoring state
  const [drivers, setDrivers] = useState<Record<string, DriverLocation>>({});
  const [trips, setTrips] = useState<TripDto[]>([]);
  const routeCache = useRef<Map<number, RouteResponse>>(new Map());
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [data, setData] = useState<MapData | null>(null);
  const [zoom, setZoom] = useState(1);
  const [offset, setOffset] = useState<Vec2>({ x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const lastPos = useRef<Vec2>({ x: 0, y: 0 });

  // Resize canvas to container
  useEffect(() => {
    function resize() {
      const canvas = canvasRef.current;
      const container = containerRef.current;
      if (!canvas || !container) return;
      const dpr = window.devicePixelRatio || 1;
      const rect = container.getBoundingClientRect();
      canvas.width = Math.floor(rect.width * dpr);
      canvas.height = Math.floor(rect.height * dpr);
      canvas.style.width = `${rect.width}px`;
      canvas.style.height = `${rect.height}px`;
      const ctx = canvas.getContext('2d');
      if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      render();
    }
    const ro = new ResizeObserver(resize);
    if (containerRef.current) ro.observe(containerRef.current);
    resize();
    return () => ro.disconnect();
  }, []);

  // Fetch map
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
      setTrips(snap.trips || []);
      setDrivers(snap.drivers || {});
    }).catch(() => {});
    ws = connectMonitoring((msg) => {
      if (msg.type === 'driver.locations') {
        setDrivers(msg.drivers || {});
      } else if (msg.type.startsWith('trip.')) {
        // simple strategy: refetch snapshot on any trip event
        fetchSnapshot().then((snap) => {
          setTrips(snap.trips || []);
        }).catch(() => {});
      }
    });
    return () => { if (ws) try { ws.close(); } catch {} };
  }, []);

  // Ensure routes fetched for accepted/started trips
  useEffect(() => {
    const need: Array<Promise<void>> = [];
    trips.forEach((t) => {
      const id = t.id;
      if (routeCache.current.has(id)) return;
      if (t.status === 'PICKING_UP') {
        const drv = t.driverId != null ? drivers[String(t.driverId)] : undefined;
        if (!drv) return;
        const req = { startPoint: { x: drv.x, y: drv.y }, destinationPoint: { x: t.startLongitude, y: t.startLatitude } };
        need.push(fetchRoute(req).then((r) => { routeCache.current.set(id, r); render(); }).catch(() => {}));
      } else if (t.status === 'STARTED') {
        const req = { startPoint: { x: t.startLongitude, y: t.startLatitude }, destinationPoint: { x: t.endLongitude, y: t.endLatitude } };
        need.push(fetchRoute(req).then((r) => { routeCache.current.set(id, r); render(); }).catch(() => {}));
      }
    });
    if (need.length) Promise.allSettled(need).then(() => render());
  }, [trips, drivers]);

  function render() {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    const size = { x: canvas.clientWidth, y: canvas.clientHeight };
    if (data) {
      drawMap(ctx, data, size, zoom, offset);
      // draw cached routes
      routeCache.current.forEach((route) => drawRoute(ctx, route, zoom, offset));
    }
    else {
      // background only
      ctx.fillStyle = BG;
      ctx.fillRect(0, 0, size.x, size.y);
    }
  }

  // Re-render on state changes
  useEffect(() => { render(); }, [data, zoom, offset]);
  useEffect(() => { render(); }, [drivers, trips]);

  // Interactions
  function onWheel(e: React.WheelEvent) {
    e.preventDefault();
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
    const worldBefore = { x: mouse.x / zoom + offset.x, y: mouse.y / zoom + offset.y };
    const delta = -e.deltaY;
    const factor = Math.exp(delta * 0.001);
    const newZoom = Math.min(5, Math.max(0.2, zoom * factor));
    const worldAfter = worldBefore;
    const newOffset = {
      x: worldAfter.x - mouse.x / newZoom,
      y: worldAfter.y - mouse.y / newZoom,
    };
    setZoom(newZoom);
    setOffset(newOffset);
  }

  function onMouseDown(e: React.MouseEvent) {
    setDragging(true);
    lastPos.current = { x: e.clientX, y: e.clientY };
  }
  function onMouseMove(e: React.MouseEvent) {
    if (!dragging) return;
    const dx = e.clientX - lastPos.current.x;
    const dy = e.clientY - lastPos.current.y;
    lastPos.current = { x: e.clientX, y: e.clientY };
    setOffset((o) => ({ x: o.x - dx / zoom, y: o.y - dy / zoom }));
  }
  function onMouseUp() { setDragging(false); }
  function onMouseLeave() { setDragging(false); }

  return (
    <div ref={containerRef} className="w-full h-full relative select-none">
      <canvas
        ref={canvasRef}
        className="w-full h-full cursor-grab active:cursor-grabbing rounded-xl shadow-inner shadow-black/40"
        onWheel={onWheel}
        onMouseDown={onMouseDown}
        onMouseMove={onMouseMove}
        onMouseUp={onMouseUp}
        onMouseLeave={onMouseLeave}
      />
      {/* UI overlay */}
      {/* Trip markers (MATCHING): rider at start point */}
      {trips.filter(t => t.status === 'MATCHING').map(t => {
        const sp = worldToScreen({ x: t.startLongitude, y: t.startLatitude }, zoom, offset);
        return (
          <div key={`match-${t.id}`} className="absolute" style={{ left: sp.x - 8, top: sp.y - 8 }}>
            <User size={16} className="text-yellow-300 drop-shadow" />
          </div>
        );
      })}
      {/* Driver icons */}
      {Object.entries(drivers).map(([id, d]) => {
        const p = worldToScreen({ x: d.x, y: d.y }, zoom, offset);
        return (
          <div key={`drv-${id}`} className="absolute" style={{ left: p.x - 7, top: p.y - 7, transform: `rotate(${d.degree}deg)` }}>
            <Car size={14} className="text-cyan-300 drop-shadow" />
          </div>
        );
      })}
      <div className="absolute top-3 left-3 bg-black/40 text-white text-xs px-2 py-1 rounded-full backdrop-blur border border-white/10">
        Map
      </div>
      <div className="absolute bottom-3 right-3 flex gap-2">
        <button
          className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20"
          onClick={() => setZoom((z) => Math.min(5, z * 1.2))}
        >+
        </button>
        <button
          className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20"
          onClick={() => setZoom((z) => Math.max(0.2, z / 1.2))}
        >-
        </button>
        <button
          className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20"
          onClick={() => {
            if (!data || !canvasRef.current) return;
            const size = { x: canvasRef.current.clientWidth, y: canvasRef.current.clientHeight };
            const fit = fitView(data.nodes, size);
            setZoom(fit.zoom);
            setOffset(fit.offset);
          }}
        >Fit
        </button>
      </div>
    </div>
  );
}
