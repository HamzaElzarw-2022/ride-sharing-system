import { useMemo } from 'react';
import { User, MapPin, Car } from 'lucide-react';
import { useMonitoring } from '../../context/MonitoringContext';

export default function SidebarTripsTab({ onOpenTrip }: { onOpenTrip: (tripId: number) => void }) {
  const { trips } = useMonitoring();

  const sorted = useMemo(() => {
    // show newest first by id as a proxy (no createdAt in TripDto)
    return [...trips].sort((a, b) => b.id - a.id);
  }, [trips]);

  return (
    <div className="p-3 overflow-auto space-y-2">
      {sorted.length === 0 && (
        <div className="p-3 rounded-lg bg-slate-800/40 border border-slate-700/40 text-xs text-slate-400">No trips</div>
      )}
      <ul className="space-y-2">
        {sorted.map(t => (
          <li
            key={t.id}
            className="p-3 rounded-lg bg-slate-800/50 border border-slate-700/50 hover:bg-slate-700/40 cursor-pointer"
            onClick={() => onOpenTrip(t.id)}
          >
            <div className="flex items-center justify-between">
              <div className="text-xs font-medium text-slate-200">Trip #{t.id}</div>
              <div className="text-[10px] px-2 py-0.5 rounded-full border border-slate-600/60 text-slate-300">
                {t.status}
              </div>
            </div>
            <div className="mt-2 text-[11px] text-slate-400 flex flex-col gap-1">
              <div className="flex items-center gap-2"><User size={12} className="text-yellow-300"/><span>Rider #{t.riderId}</span></div>
              {t.driverId != null && (
                <div className="flex items-center gap-2"><Car size={12} className="text-emerald-300"/><span>Driver #{t.driverId}</span></div>
              )}
              <div className="flex items-center gap-2"><MapPin size={12} className="text-sky-300"/><span>({t.startX.toFixed(1)}, {t.startY.toFixed(1)}) → ({t.destX.toFixed(1)}, {t.destY.toFixed(1)})</span></div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
