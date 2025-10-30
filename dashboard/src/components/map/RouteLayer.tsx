import { useCallback, useEffect, useRef } from 'react';
import type { Point, RouteResponse } from '../../services/routeService';

interface RouteLayerProps {
  route: RouteResponse| null;
  start: Point | null;
  end: Point | null;
  zoom?: number;
  offset?: Point;
}

export default function RouteLayer({ route, start, end, zoom = 13, offset = { x: 0, y: 0 } }: RouteLayerProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const draw = useCallback((ctx: CanvasRenderingContext2D) => {
    if(route === null) {
      return;
    }

    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

    // Draw route
    if (route.route.length > 0) {
      const fullRoute = [
        route.startPointProjection.projectionPoint,
        ...route.route.map((step) => ({ x: step.x, y: step.y })),
        route.destinationPointProjection.projectionPoint,
      ];
      ctx.strokeStyle = '#3b82f6';
      ctx.lineWidth = 4;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.beginPath();
      ctx.moveTo((fullRoute[0].x - offset.x) * zoom, (fullRoute[0].y - offset.y) * zoom);
      for (let i = 1; i < fullRoute.length; i++) {
        ctx.lineTo((fullRoute[i].x - offset.x) * zoom, (fullRoute[i].y - offset.y) * zoom);
      }
      ctx.stroke();
    }

    // Draw start and end points
    if (start) {
      ctx.fillStyle = '#3b82f6';
      ctx.beginPath();
      ctx.arc((start.x - offset.x) * zoom, (start.y - offset.y) * zoom, 8, 0, 2 * Math.PI);
      ctx.fill();
    }
    if (end) {
      ctx.fillStyle = '#ef4444';
      const x = (end.x - offset.x) * zoom;
      const y = (end.y - offset.y) * zoom;
      const size = 24;
      const width = size * 0.8;
      const height = size;

      ctx.beginPath();
      ctx.moveTo(x, y);
      ctx.bezierCurveTo(
        x - width / 2, y - height * 0.6,
        x - width / 2, y - height * 0.8,
        x, y - height
      );
      ctx.bezierCurveTo(
        x + width / 2, y - height * 0.8,
        x + width / 2, y - height * 0.6,
        x, y
      );
      ctx.closePath();
      ctx.fill();
    }
  }, [route, start, end, zoom, offset]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    draw(ctx);
  }, [draw]);

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
      if (ctx) {
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        draw(ctx);
      }
    }
    const ro = new ResizeObserver(resize);
    if (containerRef.current) ro.observe(containerRef.current);
    resize();
    return () => ro.disconnect();
  }, [draw]);

  return (
    <div ref={containerRef} className="w-full h-full absolute top-0 left-0 pointer-events-none">
      <canvas
        ref={canvasRef}
        className="w-full h-full"
      />
    </div>
  );
}
