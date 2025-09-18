import type { DriverLocation, TripDto } from '../../services/monitoringService';
import { worldToScreen } from './BaseMap';
import type { Vec2 } from './BaseMap';

export default function DriversLayer({ drivers, trips, zoom, offset }: { drivers: Record<string, DriverLocation>; trips: TripDto[]; zoom: number; offset: Vec2; }) {
  return (
    <>
      {Object.entries(drivers).map(([id, d]) => {
        const p = worldToScreen({ x: d.x, y: d.y }, zoom, offset);
        const driverId = Number(id);
        const myTrips = trips.filter(t => t.driverId === driverId);
        const inTrip = myTrips.some(t => t.status === 'STARTED');
        const pickingUp = !inTrip && myTrips.some(t => t.status === 'PICKING_UP');
        const color = inTrip ? '#22c55e' : pickingUp ? '#f59e0b' : '#67e8f9';
        const w = 16; const h = 10;
        return (
          <div key={`drv-${id}`} className="absolute" style={{ left: p.x - w/2, top: p.y - h/2, width: w, height: h, transform: `rotate(${d.degree}deg)`, transformOrigin: 'center' }}>
            <div className="rounded-[2px] shadow" style={{ width: '100%', height: '100%', backgroundColor: color, border: '1px solid rgba(0,0,0,0.6)' }} />
          </div>
        );
      })}
    </>
  );
}
