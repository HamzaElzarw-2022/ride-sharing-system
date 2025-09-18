import React from 'react';
import { User, Flag } from 'lucide-react';
import type { TripDto } from '../../services/monitoringService';
import { worldToScreen } from './BaseMap';
import type { Vec2 } from './BaseMap';

export default function TripMarkersLayer({ trips, zoom, offset }: { trips: TripDto[]; zoom: number; offset: Vec2; }) {
  return (
    <>
      {trips.filter(t => t.status === 'MATCHING').map(t => {
        const sp = worldToScreen({ x: t.startLongitude, y: t.startLatitude }, zoom, offset);
        return (
          <div key={`match-${t.id}`} className="absolute" style={{ left: sp.x - 8, top: sp.y - 8 }}>
            <User size={16} className="text-yellow-300 drop-shadow" />
          </div>
        );
      })}
      {trips.filter(t => t.status === 'PICKING_UP').map(t => {
        const sp = worldToScreen({ x: t.startLongitude, y: t.startLatitude }, zoom, offset);
        const dp = worldToScreen({ x: t.endLongitude, y: t.endLatitude }, zoom, offset);
        return (
          <React.Fragment key={`pu-${t.id}`}>
            <div className="absolute" style={{ left: sp.x - 4, top: sp.y - 4, width: 8, height: 8 }}>
              <div className="rounded-full" style={{ width: '100%', height: '100%', backgroundColor: '#e5e7eb', border: '2px solid #94a3b8' }} />
            </div>
            <div className="absolute" style={{ left: dp.x - 8, top: dp.y - 8 }}>
              <User size={16} className="text-yellow-300 drop-shadow" />
            </div>
          </React.Fragment>
        );
      })}
      {trips.filter(t => t.status === 'STARTED').map(t => {
        const sp = worldToScreen({ x: t.startLongitude, y: t.startLatitude }, zoom, offset);
        const dp = worldToScreen({ x: t.endLongitude, y: t.endLatitude }, zoom, offset);
        return (
          <React.Fragment key={`st-${t.id}`}>
            <div className="absolute" style={{ left: sp.x - 4, top: sp.y - 4, width: 8, height: 8 }}>
              <div className="rounded-full" style={{ width: '100%', height: '100%', backgroundColor: '#e5e7eb', border: '2px solid #94a3b8' }} />
            </div>
            <div className="absolute" style={{ left: dp.x - 7, top: dp.y - 10 }}>
              <Flag size={14} className="text-red-400 drop-shadow" />
            </div>
          </React.Fragment>
        );
      })}
    </>
  );
}
