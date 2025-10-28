import React from 'react';
import type { Point, RouteStep } from '../../services/routeService';

interface RouteLayerProps {
  canvasRef: React.RefObject<HTMLCanvasElement>;
  route: RouteStep[];
  start: Point | null;
  end: Point | null;
  zoom: number;
  offset: Point;
}

export default function RouteLayer({ canvasRef, route, start, end, zoom, offset }: RouteLayerProps) {
  const draw = (ctx: CanvasRenderingContext2D) => {
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

    // Draw route
    if (route.length > 0) {
      ctx.strokeStyle = '#3b82f6';
      ctx.lineWidth = 4;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.beginPath();
      ctx.moveTo((route[0].x - offset.x) * zoom, (route[0].y - offset.y) * zoom);
      for (let i = 1; i < route.length; i++) {
        ctx.lineTo((route[i].x - offset.x) * zoom, (route[i].y - offset.y) * zoom);
      }
      ctx.stroke();
    }

    // Draw start and end points
    if (start) {
      ctx.fillStyle = '#10b981';
      ctx.beginPath();
      ctx.arc((start.x - offset.x) * zoom, (start.y - offset.y) * zoom, 8, 0, 2 * Math.PI);
      ctx.fill();
    }
    if (end) {
      ctx.fillStyle = '#ef4444';
      ctx.beginPath();
      ctx.arc((end.x - offset.x) * zoom, (end.y - offset.y) * zoom, 8, 0, 2 * Math.PI);
      ctx.fill();
    }
  };

  React.useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    draw(ctx);
  }, [route, start, end, zoom, offset]);

  return null;
}
