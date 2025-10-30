import React, { useEffect, useRef, useState } from 'react';
import type { Vec2 } from './map/BaseMap';
import { fetchMap } from '../services/mapService';
import type { MapData } from '../services/mapService';
import { useMapInteractions } from '../hooks/useMapInteractions';
import type { FitType } from './map/BaseMap';

interface MapContainerProps {
  children?: React.ReactNode;
  onMapClick?: (point: Vec2) => void;
  fitType?: FitType;
}

export default function MapContainer({ children, onMapClick, fitType }: MapContainerProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
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
  } = useMapInteractions(data, containerRef, fitType);

  // Fetch map
  useEffect(() => {
    fetchMap()
      .then(setData)
      .catch(() => {});
  }, []);

  const handleCanvasClick = (e: React.MouseEvent) => {
    if (onMapClick && containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      const mouse = { x: e.clientX - rect.left, y: e.clientY - rect.top };
      const worldPoint = { x: mouse.x / zoom + offset.x, y: mouse.y / zoom + offset.y };
      onMapClick(worldPoint);
    }
  };

  return (
    <div 
      ref={containerRef} 
      className="w-full h-full relative select-none cursor-grab active:cursor-grabbing z-10"
      onWheel={onWheel}
      onMouseDown={onMouseDown}
      onMouseMove={onMouseMove}
      onMouseUp={onMouseUp}
      onMouseLeave={onMouseLeave}
      onClick={handleCanvasClick}
    >
      {React.Children.map(children, (child) => {
        if (React.isValidElement(child)) {
          return React.cloneElement(child as React.ReactElement<any>, {
            data,
            zoom,
            offset,
            dragging,
          });
        }
        return child;
      })}
      <div className="absolute bottom-3 right-3 flex gap-2">
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => setZoom((z) => Math.min(5, z * 1.2))}>+</button>
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => setZoom((z) => Math.max(0.2, z / 1.2))}>-</button>
        <button className="px-2 py-1 rounded bg-white/10 text-white border border-white/20 hover:bg-white/20" onClick={() => fitMap(fitType)}>Fit</button>
      </div>
    </div>
  );
}
