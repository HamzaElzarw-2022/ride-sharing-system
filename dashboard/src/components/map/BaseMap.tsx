import React, { useEffect, useRef, useCallback } from 'react';
import type { MapData, MapNode } from '../../services/mapService';
import { drawPolygons } from './polygonRenderer';

export type Vec2 = { x: number; y: number };

// eslint-disable-next-line react-refresh/only-export-components
export function fitView(nodes: MapNode[], canvasSize: Vec2): { zoom: number; offset: Vec2 } {
  if (!nodes.length) return { zoom: 1, offset: { x: 0, y: 0 } };
  const minX = Math.min(...nodes.map(n => n.x));
  const minY = Math.min(...nodes.map(n => n.y));
  const maxX = Math.max(...nodes.map(n => n.x));
  const maxY = Math.max(...nodes.map(n => n.y));
  // Use smaller, responsive padding to reduce empty space on sides
  const padding = Math.max(8, Math.min(24, Math.floor(Math.min(canvasSize.x, canvasSize.y) * 0.01)));
  const width = maxX - minX || 1;
  const height = maxY - minY || 1;
  const zoomX = (canvasSize.x - padding * 2) / width;
  const zoomY = (canvasSize.y - padding * 2) / height;
  const zoom = Math.max(0.1, Math.min(zoomX, zoomY));
  const offset: Vec2 = { x: minX - padding / zoom, y: minY - padding / zoom };
  return { zoom, offset };
}

// eslint-disable-next-line react-refresh/only-export-components
export function worldToScreen(p: Vec2, zoom: number, offset: Vec2): Vec2 {
  return { x: (p.x - offset.x) * zoom, y: (p.y - offset.y) * zoom };
}

// Dark palette (Google Maps dark-inspired)
const BG = '#0B0F14'; // deep neutral background
const ROAD_CASING = '#0F151C'; // darker edge to carve roads
const ROAD_FILL = '#232C36'; // default local street

// Speed tiers for road fill to replace center-line visual cue
function roadFillFor(speed: number): string {
  // On dark maps, faster roads are slightly lighter for hierarchy
  if (speed >= 90) return '#4A5562'; // highways
  if (speed >= 70) return '#3B4653'; // fast arterials
  if (speed >= 50) return '#2E3946'; // collectors
  return ROAD_FILL; // locals
}

// // Clean light palette (Google/Uber-inspired)
// const BG = '#F5F7FA'; // canvas background
// const ROAD_CASING = '#C9D1D9'; // subtle road edge
// const ROAD_FILL = '#FFFFFF'; // default road fill

// // Speed tiers for road fill to replace center-line visual cue
// function roadFillFor(speed: number): string {
//   if (speed >= 90) return '#DCE4EC'; // highways: darker neutral
//   if (speed >= 70) return '#E7EDF3'; // fast arterials
//   if (speed >= 50) return '#F2F5F8'; // collectors
//   return '#FFFFFF'; // locals
// }

function drawMap(ctx: CanvasRenderingContext2D, data: MapData, size: Vec2, zoom: number, offset: Vec2) {
  // background
  ctx.fillStyle = BG; ctx.fillRect(0, 0, size.x, size.y);
  // vignette
  const grad = ctx.createRadialGradient(size.x/2, size.y/2, Math.min(size.x,size.y)*0.25, size.x/2, size.y/2, Math.max(size.x,size.y)*0.85);
  grad.addColorStop(0, 'rgba(0,0,0,0)'); grad.addColorStop(1, 'rgba(0,0,0,0.25)');
  ctx.fillStyle = grad; ctx.fillRect(0, 0, size.x, size.y);

  // draw terrain polygons behind streets
  drawPolygons(ctx, data, zoom, offset);

  const nodeById: Record<number, MapNode> = Object.fromEntries(data.nodes.map(n => [n.id, n]));
  ctx.lineCap = 'round';
  // Ensure faster streets render on top: draw slower first, faster last
  const sortedEdges = [...data.edges].sort((a, b) => (a.speed ?? 0) - (b.speed ?? 0));
  
  // Draw all road casings first
  ctx.strokeStyle = ROAD_CASING;
  for (const e of sortedEdges) {
    const a = nodeById[e.startId]; const b = nodeById[e.endId]; if (!a || !b) continue;
    const A = worldToScreen(a, zoom, offset); const B = worldToScreen(b, zoom, offset);
    const baseWidth = 6; const speedFactor = Math.min(1.6, Math.max(0.8, e.speed / 50));
    const roadWidth = Math.max(3, baseWidth * speedFactor * zoom); const casingWidth = roadWidth + Math.max(2, 2.5 * zoom);
    ctx.lineWidth = casingWidth;
    ctx.beginPath(); ctx.moveTo(A.x, A.y); ctx.lineTo(B.x, B.y); ctx.stroke();
  }
  
  // Draw all road fills second (color varies by speed)
  for (const e of sortedEdges) {
    const a = nodeById[e.startId]; const b = nodeById[e.endId]; if (!a || !b) continue;
    const A = worldToScreen(a, zoom, offset); const B = worldToScreen(b, zoom, offset);
    const baseWidth = 6; const speedFactor = Math.min(1.6, Math.max(0.8, e.speed / 50));
    const roadWidth = Math.max(3, baseWidth * speedFactor * zoom);
    ctx.strokeStyle = roadFillFor(e.speed) || ROAD_FILL;
    ctx.lineWidth = roadWidth;
    ctx.beginPath(); ctx.moveTo(A.x, A.y); ctx.lineTo(B.x, B.y); ctx.stroke();
  }
  // Intersections are now rendered as part of the road network
  // No separate rendering needed as roads naturally connect at nodes
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

  const render = useCallback(() => {
    const canvas = canvasRef.current; if (!canvas) return;
    const ctx = canvas.getContext('2d'); if (!ctx) return;
    const size = { x: canvas.clientWidth, y: canvas.clientHeight };
    if (data) {
      drawMap(ctx, data, size, zoom, offset);
    } else {
      ctx.fillStyle = BG; ctx.fillRect(0, 0, size.x, size.y);
    }
    // notify listeners that BaseMap finished rendering
  try { window.dispatchEvent(new CustomEvent('basemap-rendered')); } catch { /* noop */ }
  }, [canvasRef, data, zoom, offset]);

  useEffect(() => { render(); }, [render]);

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
  }, [canvasRef, render]);

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
