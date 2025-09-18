import { useEffect, useRef } from 'react';
import type { DriverLocation, TripDto } from '../../services/monitoringService';
import { worldToScreen } from './BaseMap';
import type { Vec2 } from './BaseMap';

export default function DriversLayer({ drivers, trips, zoom, offset, dragging = false }: { drivers: Record<string, DriverLocation>; trips: TripDto[]; zoom: number; offset: Vec2; dragging?: boolean; }) {
  // Avoid animating on first paint
  const firstPaintRef = useRef(true);
  useEffect(() => {
    firstPaintRef.current = false;
  }, []);

  return (
    <>
      {Object.entries(drivers).map(([id, d]) => {
        const p = worldToScreen({ x: d.x, y: d.y }, zoom, offset);
        const driverId = Number(id);

        const myTrips = trips.filter(t => t.driverId === driverId);
        const inTrip = myTrips.some(t => t.status === 'STARTED');
        const pickingUp = !inTrip && myTrips.some(t => t.status === 'PICKING_UP');

        const color = inTrip ? '#10b981' : pickingUp ? '#3b82f6' : '#7d7d7d';
        const w = 30; const h = 15;

        // Position via CSS transform for smooth transitions
        const translateRotate = `translate3d(${p.x - w / 2}px, ${p.y - h / 2}px, 0) rotate(${d.degree}deg)`;
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
          </div>
        );
      })}
    </>
  );
}
