import { useState, useRef, useEffect, useCallback } from 'react';
import type { MapData } from '../services/mapService';
import { fitView, type Vec2 } from '../components/map/BaseMap';

export function useMapInteractions(data: MapData | null, containerRef: React.RefObject<HTMLElement>) {
  const view = useRef({ zoom: 1, offset: { x: 0, y: 0 } });
  const [liveView, setLiveView] = useState(view.current);
  const [dragging, setDragging] = useState(false);
  const lastPos = useRef<Vec2>({ x: 0, y: 0 });

  const setView = useCallback((newView: { zoom: number, offset: Vec2 }, isLive = false) => {
    view.current = newView;
    if (isLive) {
      setLiveView(newView);
    }
  }, []);

  // Fit view on initial data load
  useEffect(() => {
    if (data && containerRef.current) {
      const size = { x: containerRef.current.clientWidth, y: containerRef.current.clientHeight };
      if (size.x > 0 && size.y > 0) {
        const fit = fitView(data.nodes, size);
        setView(fit, true);
      }
    }
  }, [data, containerRef, setView]);

  function onWheel(e: React.WheelEvent) {
    e.preventDefault();
    const container = containerRef.current; if (!container) return;
    const rect = container.getBoundingClientRect();
    const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
    const worldBefore = { x: mouse.x / view.current.zoom + view.current.offset.x, y: mouse.y / view.current.zoom + view.current.offset.y };
    const delta = -e.deltaY;
    const factor = Math.exp(delta * 0.001);
    const newZoom = Math.min(5, Math.max(0.2, view.current.zoom * factor));
    const newOffset = { x: worldBefore.x - mouse.x / newZoom, y: worldBefore.y - mouse.y / newZoom };
    setView({ zoom: newZoom, offset: newOffset }, true);
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
    setView({
      zoom: view.current.zoom,
      offset: { x: view.current.offset.x - dx / view.current.zoom, y: view.current.offset.y - dy / view.current.zoom },
    }, true);
  }

  function onMouseUp() {
    setDragging(false);
  }

  function onMouseLeave() {
    if (dragging) {
      setDragging(false);
    }
  }

  function fitMap() {
    if (!data || !containerRef.current) return;
    const size = { x: containerRef.current.clientWidth, y: containerRef.current.clientHeight };
    const fit = fitView(data.nodes, size);
    setView(fit, true);
  }

  const setZoom = useCallback((updater: (z: number) => number) => {
    const newZoom = updater(view.current.zoom);
    setView({ ...view.current, zoom: newZoom }, true);
  }, [setView]);

  return {
    zoom: liveView.zoom,
    offset: liveView.offset,
    dragging,
    onWheel,
    onMouseDown,
    onMouseMove,
    onMouseUp,
    onMouseLeave,
    fitMap,
    setZoom,
  };
}
