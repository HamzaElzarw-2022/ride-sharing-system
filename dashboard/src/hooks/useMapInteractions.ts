import { useState, useRef, useEffect } from 'react';
import type { MapData } from '../services/mapService';
import { fitView, type Vec2 } from '../components/map/BaseMap';

export function useMapInteractions(data: MapData | null, canvasRef: React.RefObject<HTMLCanvasElement>) {
  const [zoom, setZoom] = useState(1);
  const [offset, setOffset] = useState<Vec2>({ x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const lastPos = useRef<Vec2>({ x: 0, y: 0 });

  // Fit view on initial data load
  useEffect(() => {
    if (data && canvasRef.current) {
      const size = { x: canvasRef.current.clientWidth, y: canvasRef.current.clientHeight };
      const fit = fitView(data.nodes, size);
      setZoom(fit.zoom);
      setOffset(fit.offset);
    }
  }, [data, canvasRef]);

  function onWheel(e: React.WheelEvent) {
    e.preventDefault();
    const canvas = canvasRef.current; if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
    const worldBefore = { x: mouse.x / zoom + offset.x, y: mouse.y / zoom + offset.y };
    const delta = -e.deltaY;
    const factor = Math.exp(delta * 0.001);
    const newZoom = Math.min(5, Math.max(0.2, zoom * factor));
    const newOffset = { x: worldBefore.x - mouse.x / newZoom, y: worldBefore.y - mouse.y / newZoom };
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
    setOffset((o: Vec2) => ({ x: o.x - dx / zoom, y: o.y - dy / zoom }));
  }

  function onMouseUp() {
    setDragging(false);
  }

  function onMouseLeave() {
    setDragging(false);
  }

  function fitMap() {
    if (!data || !canvasRef.current) return;
    const size = { x: canvasRef.current.clientWidth, y: canvasRef.current.clientHeight };
    const fit = fitView(data.nodes, size);
    setZoom(fit.zoom);
    setOffset(fit.offset);
  }

  return {
    zoom,
    offset,
    dragging,
    onWheel,
    onMouseDown,
    onMouseMove,
    onMouseUp,
    onMouseLeave,
    fitMap,
    setZoom,
    setOffset,
  };
}
