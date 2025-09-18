import React, { useEffect, useRef } from 'react';
import type { MapData, MapNode } from '../../services/mapService';

export type Vec2 = { x: number; y: number };

export function fitView(nodes: MapNode[], canvasSize: Vec2): { zoom: number; offset: Vec2 } {
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

export function worldToScreen(p: Vec2, zoom: number, offset: Vec2): Vec2 {
  return { x: (p.x - offset.x) * zoom, y: (p.y - offset.y) * zoom };
}

// Colors moved locally
const BG = '#0b1220';
const GRID = '#101826';
const ROAD_CASING = '#0a0f1a';
const ROAD_FILL = '#1e293b';
const ROAD_CENTER = '#facc15';
const ROAD_CENTER_ALT = '#94a3b8';
const INTERSECTION_HALO = 'rgba(255,255,255,0.06)';

function drawGrid(ctx: CanvasRenderingContext2D, width: number, height: number, zoom: number, offset: Vec2) {
  const step = 50 * zoom;
  ctx.save();
  ctx.strokeStyle = GRID;
  ctx.lineWidth = 1;

  const startX = -((offset.x * zoom) % step);
  const startY = -((offset.y * zoom) % step);
  for (let x = startX; x < width; x += step) {
    ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, height); ctx.stroke();
  }
  for (let y = startY; y < height; y += step) {
    ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(width, y); ctx.stroke();
  }
  ctx.restore();
}

function drawMap(ctx: CanvasRenderingContext2D, data: MapData, size: Vec2, zoom: number, offset: Vec2) {
  // background
  ctx.fillStyle = BG; ctx.fillRect(0, 0, size.x, size.y);
  drawGrid(ctx, size.x, size.y, zoom, offset);
  // vignette
  const grad = ctx.createRadialGradient(size.x/2, size.y/2, Math.min(size.x,size.y)*0.2, size.x/2, size.y/2, Math.max(size.x,size.y)*0.7);
  grad.addColorStop(0, 'rgba(0,0,0,0)'); grad.addColorStop(1, 'rgba(0,0,0,0.25)');
  ctx.fillStyle = grad; ctx.fillRect(0, 0, size.x, size.y);

  const nodeById: Record<number, MapNode> = Object.fromEntries(data.nodes.map(n => [n.id, n]));
  ctx.lineCap = 'round';
  for (const e of data.edges) {
    const a = nodeById[e.startId]; const b = nodeById[e.endId]; if (!a || !b) continue;
    const A = worldToScreen(a, zoom, offset); const B = worldToScreen(b, zoom, offset);
    const baseWidth = 6; const speedFactor = Math.min(1.6, Math.max(0.8, e.speed / 50));
    const roadWidth = Math.max(3, baseWidth * speedFactor * zoom); const casingWidth = roadWidth + Math.max(2, 2.5 * zoom);
    // casing
    ctx.strokeStyle = ROAD_CASING; ctx.lineWidth = casingWidth; ctx.beginPath(); ctx.moveTo(A.x, A.y); ctx.lineTo(B.x, B.y); ctx.stroke();
    // fill
    ctx.strokeStyle = ROAD_FILL; ctx.lineWidth = roadWidth; ctx.beginPath(); ctx.moveTo(A.x, A.y); ctx.lineTo(B.x, B.y); ctx.stroke();
    // center line
    const centerColor = e.speed >= 70 ? ROAD_CENTER : ROAD_CENTER_ALT; const dash = Math.max(6, 10 * zoom); const gap = dash * 0.8;
    ctx.save(); ctx.strokeStyle = centerColor; ctx.lineWidth = Math.max(1, 1.5 * zoom); ctx.setLineDash([dash, gap]);
    ctx.beginPath(); ctx.moveTo(A.x, A.y); ctx.lineTo(B.x, B.y); ctx.stroke(); ctx.restore();
  }
  for (const n of data.nodes) {
    const P = worldToScreen(n, zoom, offset); const r = Math.max(3, 5 * zoom);
    ctx.fillStyle = INTERSECTION_HALO; ctx.beginPath(); ctx.arc(P.x, P.y, r*1.4, 0, Math.PI*2); ctx.fill();
    ctx.fillStyle = ROAD_FILL; ctx.beginPath(); ctx.arc(P.x, P.y, r, 0, Math.PI*2); ctx.fill();
  }
}

export type BaseMapProps = {
  children: React.ReactNode;
  data: MapData | null;
  zoom: number;
  offset: Vec2;
  onWheel: (e: React.WheelEvent) => void;
  onMouseDown: (e: React.MouseEvent) => void;
  onMouseMove: (e: React.MouseEvent) => void;
  onMouseUp: (e: React.MouseEvent) => void;
  onMouseLeave: (e: React.MouseEvent) => void;
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
};

export default function BaseMap(props: BaseMapProps) {
  const { data, zoom, offset, onWheel, onMouseDown, onMouseMove, onMouseUp, onMouseLeave, canvasRef } = props;
  const containerRef = useRef<HTMLDivElement | null>(null);

  function render() {
    const canvas = canvasRef.current; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const size = { x: canvas.clientWidth, y: canvas.clientHeight };
    if (data) {
      drawMap(ctx, data, size, zoom, offset);
    } else {
      ctx.fillStyle = BG; ctx.fillRect(0, 0, size.x, size.y);
    }
    // notify listeners that BaseMap finished rendering
    try { window.dispatchEvent(new CustomEvent('basemap-rendered')); } catch {}
  }

  useEffect(() => { render(); }, [data, zoom, offset]);

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
  }, []);

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
      {props.children}
    </div>
  );
}
