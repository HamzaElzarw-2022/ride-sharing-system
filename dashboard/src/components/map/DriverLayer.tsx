import { useEffect, useRef } from 'react';
import type { Point } from '../../services/routeService';
import { worldToScreen } from './BaseMap';
import type { Vec2 } from './BaseMap';

type DriverLocation = Point & { degree: number };

export default function DriverLayer({ position, zoom, offset, dragging = false }: { position: DriverLocation | null; zoom: number; offset: Vec2; dragging?: boolean; }) {
  const firstPaintRef = useRef(true);
  const prevAngleRef = useRef<number | null>(null);

  useEffect(() => {
    firstPaintRef.current = false;
  }, []);

  if (!position) {
    return null;
  }

  const p = worldToScreen({ x: position.x, y: position.y }, zoom, offset);
  
  const color = '#7d7d7d';
  
  const baseW = 30; const baseH = 17;
  const w = baseW * zoom; 
  const h = baseH * zoom;

  const normalizeAngleTowards = (prev: number, target: number) => {
    let adjusted = target;
    const diff = adjusted - prev;
    const wrappedDiff = ((((diff + 180) % 360) + 360) % 360) - 180;
    adjusted = prev + wrappedDiff;
    return adjusted;
  };

  const prev = prevAngleRef.current;
  const target = position.degree;
  const displayAngle = prev == null || firstPaintRef.current || dragging
    ? target
    : normalizeAngleTowards(prev, target);
  prevAngleRef.current = displayAngle;

  const translateRotate = `translate3d(${p.x - w / 2}px, ${p.y - h / 2}px, 0) rotate(${displayAngle}deg)`;
  const noAnim = dragging || firstPaintRef.current;

  return (
    <div
      className={[
        'absolute top-0 left-0',
        'will-change-transform',
        noAnim ? 'transition-none' : 'transition-transform duration-[980ms] ease-linear',
        'pointer-events-none',
      ].join(' ')}
      style={{
        width: w,
        height: h,
        transform: translateRotate,
        transformOrigin: 'center',
      }}
    >
      <div className="rounded-sm shadow" style={{ width: '100%', height: '100%', backgroundColor: color }} />
    </div>
  );
}
