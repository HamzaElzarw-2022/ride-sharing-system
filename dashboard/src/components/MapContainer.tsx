import React, { useEffect, useRef, useState } from 'react';
import BaseMap, { fitView, type Vec2 } from './map/BaseMap';
import { fetchMap } from '../services/mapService';
import type { MapData } from '../services/mapService';
import { useMapInteractions } from '../hooks/useMapInteractions';

interface MapContainerProps {
  children?: React.ReactNode;
  onMapClick?: (point: Vec2) => void;
}

export default function MapContainer({ children, onMapClick }: MapContainerProps) {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [data, setData] = useState<MapData | null>(null);
  const {
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
  } = useMapInteractions(data, canvasRef);

  // Fetch map
  useEffect(() => {
    fetchMap()
      .then(setData)
      .catch(() => {});
  }, []);

  const handleCanvasClick = (e: React.MouseEvent) => {
    if (onMapClick && canvasRef.current) {
      const rect = canvasRef.current.getBoundingClientRect();
      const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
      const worldPoint = { x: mouse.x / zoom + offset.x, y: mouse.y / zoom + offset.y };
      onMapClick(worldPoint);
    }
  };

  return (
    <BaseMap
      data={data}
      zoom={zoom}
      offset={offset}
      onWheel={onWheel}
      onMouseDown={onMouseDown}
      onMouseMove={onMouseMove}
      onMouseUp={onMouseUp}
      onMouseLeave={onMouseLeave}
      onClick={handleCanvasClick}
      canvasRef={canvasRef}
    >
      {React.Children.map(children, (child) => {
        if (React.isValidElement(child)) {
          return React.cloneElement(child as React.ReactElement<any>, {
            canvasRef,
            zoom,
            offset,
            dragging,
          });
        }
        return child;
      })}
      <div className="absolute top-3 left-3 bg-black/40 text-white text-xs px-2 py-1 rounded-full backdrop-blur border border-white/10">Map</div>
      <div className="absolute bottom-3 right-3 flex gap-2">
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => setZoom((z) => Math.min(5, z * 1.2))}>+</button>
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => setZoom((z) => Math.max(0.2, z / 1.2))}>-</button>
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={fitMap}>Fit</button>
      </div>
    </BaseMap>
  );
}
