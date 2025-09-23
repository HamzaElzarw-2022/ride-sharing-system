import { useEffect, useRef } from 'react';
import { User } from 'lucide-react';
import type { DriverLocation, TripDto } from '../../services/monitoringService';
import { worldToScreen } from './BaseMap';
import type { Vec2 } from './BaseMap';

export default function DriversLayer({ drivers, trips, zoom, offset, dragging = false }: { drivers: Record<string, DriverLocation>; trips: TripDto[]; zoom: number; offset: Vec2; dragging?: boolean; }) {
  // Avoid animating on first paint
  const firstPaintRef = useRef(true);
  // Keep a stable map of previous displayed angles per driver to ensure minimal rotation
  const prevAngleRef = useRef<Map<string, number>>(new Map());

  useEffect(() => {
    firstPaintRef.current = false;
  }, []);

  // Normalize a target angle (in degrees) to be the closest equivalent to prev
  const normalizeAngleTowards = (prev: number, target: number) => {
    // bring target close to prev by adding/subtracting 360
    let adjusted = target;
    const diff = adjusted - prev;
    // Wrap diff into (-180, 180]
    const wrappedDiff = ((((diff + 180) % 360) + 360) % 360) - 180;
    adjusted = prev + wrappedDiff;
    return adjusted;
  };

  return (
    <>
      {Object.entries(drivers).map(([id, d]) => {
        const p = worldToScreen({ x: d.x, y: d.y }, zoom, offset);
        const driverId = Number(id);

        const myTrips = trips.filter(t => t.driverId === driverId);
        const inTrip = myTrips.some(t => t.status === 'STARTED');
        const pickingUp = !inTrip && myTrips.some(t => t.status === 'PICKING_UP');

        const color = inTrip ? '#10b981' : pickingUp ? '#3b82f6' : '#7d7d7d';
        
        // Scale car size with zoom and make inactive drivers smaller
        const baseW = 22; const baseH = 11;
        const isActive = inTrip || pickingUp;
        const sizeMultiplier = isActive ? 1 : 0.9; // Inactive drivers are 30% smaller
        const w = baseW * zoom * sizeMultiplier; 
        const h = baseH * zoom * sizeMultiplier;

        // Determine rotation with minimal change from previous angle for this driver
        const prev = prevAngleRef.current.get(id);
        const target = d.degree; // incoming heading in degrees (0..360 or any)
        const displayAngle = prev == null || firstPaintRef.current || dragging
          ? target
          : normalizeAngleTowards(prev, target);
        // store for next render
        prevAngleRef.current.set(id, displayAngle);

        // Position via CSS transform for smooth transitions
        const translateRotate = `translate3d(${p.x - w / 2}px, ${p.y - h / 2}px, 0) rotate(${displayAngle}deg)`;
        const noAnim = dragging || firstPaintRef.current;

        return (
          <div
            key={`drv-${id}`}
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
            <div className="rounded-[2px] shadow" style={{ width: '100%', height: '100%', backgroundColor: color }} />
            {/* Show user icon for drivers who have picked up passengers */}
            {inTrip && (
              <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
                <User size={Math.max(8, 12 * zoom)} className="text-white drop-shadow rotate-90" />
              </div>
            )}
          </div>
        );
      })}
    </>
  );
}
