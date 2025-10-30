import { useState, useRef, useEffect, useCallback, type RefObject } from 'react';
import type { MapData } from '../services/mapService';
import { fitView, type FitType, type Vec2 } from '../components/map/BaseMap';

export const useMapInteractions = (
  data: MapData | null,
  containerRef: RefObject<HTMLElement | null>,
  fitType: FitType = 'normal'
) => {
  const [zoom, setZoom] = useState(13);
  const [offset, setOffset] = useState<Vec2>({ x: 0, y: 0 });
  const [dragging, setDragging] = useState(false);
  const lastPos = useRef<Vec2>({ x: 0, y: 0 });

  const setView = useCallback((newView: { zoom: number; offset: Vec2 }) => {
    setZoom(newView.zoom);
    setOffset(newView.offset);
  }, []);

  // Fit view on initial data load
  useEffect(() => {
    if (data && containerRef.current) {
      const size = { x: containerRef.current.clientWidth, y: containerRef.current.clientHeight };
      if (size.x > 0 && size.y > 0) {
        const fit = fitView(data.nodes, size, fitType);
        setView(fit);
      }
    }
  }, [data, containerRef, setView, fitType]);

  function onWheel(e: React.WheelEvent) {
    e.preventDefault();
    const container = containerRef.current; if (!container) return;
    const rect = container.getBoundingClientRect();
    const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
    const worldBefore = { x: mouse.x / zoom + offset.x, y: mouse.y / zoom + offset.y };
    const delta = -e.deltaY;
    const factor = Math.exp(delta * 0.001);
    const newZoom = Math.min(5, Math.max(0.2, zoom * factor));
    const newOffset = { x: worldBefore.x - mouse.x / newZoom, y: worldBefore.y - mouse.y / newZoom };
    setView({ zoom: newZoom, offset: newOffset });
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
      zoom: zoom,
      offset: { x: offset.x - dx / zoom, y: offset.y - dy / zoom },
    });
  }

  function onMouseUp() {
    setDragging(false);
  }

  function onMouseLeave() {
    if (dragging) {
      setDragging(false);
    }
  }

  function fitMap(fitType: FitType = 'normal') {
    if (!data || !containerRef.current) return;
    const size = { x: containerRef.current.clientWidth, y: containerRef.current.clientHeight };
    const fit = fitView(data.nodes, size, fitType);
    setView(fit);
  }

  const updateZoom = useCallback((updater: (z: number) => number) => {
    setZoom(updater);
  }, []);

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
    setZoom: updateZoom,
  };
};
