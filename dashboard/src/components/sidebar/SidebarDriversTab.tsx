import { useMemo } from 'react';
import { Car, Navigation2 } from 'lucide-react';
import { useMonitoring } from '../../context/MonitoringContext';

export default function SidebarDriversTab() {
  const { drivers, trips } = useMonitoring();

  const items = useMemo(() => {
    // Convert drivers record to array and sort by numeric id
    const list = Object.entries(drivers).map(([id, d]) => ({ id: Number(id), d }));
    list.sort((a, b) => a.id - b.id);
    return list.map(({ id, d }) => {
      const myTrips = trips.filter(t => t.driverId === id);
      const status = myTrips.find(t => t.status === 'STARTED') ? 'STARTED'
        : myTrips.find(t => t.status === 'PICKING_UP') ? 'PICKING_UP'
        : null;
      const color = status === 'STARTED' ? '#10b981' : status === 'PICKING_UP' ? '#3b82f6' : '#9ca3af';
      return { id, d, status, color } as const;
    });
  }, [drivers, trips]);

  return (
    <div className="p-3 overflow-auto space-y-2">
      {items.length === 0 && (
        <div className="p-3 rounded-lg bg-slate-800/40 border border-slate-700/40 text-xs text-slate-400">No drivers online</div>
      )}
      <ul className="space-y-2">
        {items.map(({ id, d, status, color }) => (
          <li key={id} className="p-3 rounded-lg bg-slate-800/50 border border-slate-700/50">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-slate-200 text-sm">
                <Car size={16} style={{ color }} />
                <span>Driver #{id}</span>
              </div>
              {status && (
                <span className="text-[10px] px-2 py-0.5 rounded-full border border-slate-600/60 text-slate-300">{status}</span>
              )}
            </div>
            <div className="mt-2 text-[11px] text-slate-400 flex flex-wrap gap-x-4 gap-y-1">
              <div className="flex items-center gap-1">
                <span className="text-slate-500">x:</span>
                <span>{d.x.toFixed(1)}</span>
              </div>
              <div className="flex items-center gap-1">
                <span className="text-slate-500">y:</span>
                <span>{d.y.toFixed(1)}</span>
              </div>
              <div className="flex items-center gap-1">
                <Navigation2 size={12} className="text-slate-400" />
                <span>{d.degree.toFixed(0)}°</span>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
