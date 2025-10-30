import { useMemo } from 'react';
import { ArrowLeft, Car, Flag, Map, MapPin, User } from 'lucide-react';
import { useMonitoring } from '../../context/MonitoringContext';

export default function SidebarTripDetailsTab({ onBack }: { onBack: () => void }) {
  const { trips, selectedTripId, routes } = useMonitoring();
  const trip = useMemo(() => trips.find(t => t.id === selectedTripId) || null, [trips, selectedTripId]);
  const route = selectedTripId != null ? routes.get(selectedTripId) : undefined;

  if (!trip) {
    return (
      <div className="p-4 text-sm text-slate-400">
        <button onClick={onBack} className="mb-3 inline-flex items-center gap-1 text-slate-300 hover:text-white">
          <ArrowLeft size={14}/> Back
        </button>
        <div>
          No trip selected.
        </div>

      </div>
    );
  }

  function fmt(n?: number) { return typeof n === 'number' ? n.toFixed(2) : '-'; }

  return (
    <div className="flex flex-col h-full">
      <div className="p-3 border-b border-slate-800 flex items-center gap-2">
        <button onClick={onBack} className="p-1 rounded hover:bg-slate-800">
          <ArrowLeft size={16} />
        </button>
        <div className="text-sm font-semibold text-slate-300">Trip #{trip.id}</div>
        <div className="ml-auto text-[10px] px-2 py-0.5 rounded-full border border-slate-600/60 text-slate-300">
          {trip.status}
        </div>
      </div>
      <div className="p-3 space-y-3 overflow-auto custom-scrollbar">
        <div className="text-[12px] text-slate-300 flex items-center gap-2"><User size={14} className="text-yellow-300"/> Rider #{trip.riderId}</div>
        {trip.driverId != null && (
          <div className="text-[12px] text-slate-300 flex items-center gap-2"><Car size={14} className="text-emerald-300"/> Driver #{trip.driverId}</div>
        )}
        <div className="text-[12px] text-slate-400 flex items-center gap-2"><MapPin size={14} className="text-sky-300"/> Start: ({trip.startX.toFixed(2)}, {trip.startY.toFixed(2)})</div>
        <div className="text-[12px] text-slate-400 flex items-center gap-2"><Flag size={14} className="text-rose-300"/> End: ({trip.destX.toFixed(2)}, {trip.destY.toFixed(2)})</div>
        <div className="pt-2 border-t border-slate-800">
          <div className="text-xs font-semibold text-slate-400 mb-2 flex items-center gap-2"><Map size={14}/> Route</div>
          {route ? (
            <div className="text-[11px] text-slate-300 space-y-3">
              {/* Projection points section */}
              <div className="grid grid-cols-1 gap-2">
                <div className="rounded border border-slate-800/80 bg-slate-900/30">
                  <div className="px-2 py-1.5 text-[10px] uppercase tracking-wide text-slate-400 border-b border-slate-800">Start projection</div>
                  <div className="px-2 py-2 space-y-1 text-slate-400">
                    <div className="flex items-center gap-2">
                      <span className="text-slate-300">Edge:</span>
                      <span className="text-slate-200">{route.startPointProjection.edge?.name ?? 'N/A'}</span>
                      <span className="ml-auto text-slate-500">#{route.startPointProjection.edge?.id}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-slate-300">Distance from start:</span>
                      <span className="text-sky-300">{fmt(route.startPointProjection.distanceFromStart)} m</span>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div className="text-slate-400">Original: ({fmt(route.startPointProjection.originalPoint?.x)}, {fmt(route.startPointProjection.originalPoint?.y)})</div>
                      <div className="text-slate-400">Projection: ({fmt(route.startPointProjection.projectionPoint?.x)}, {fmt(route.startPointProjection.projectionPoint?.y)})</div>
                    </div>
                  </div>
                </div>

                <div className="rounded border border-slate-800/80 bg-slate-900/30">
                  <div className="px-2 py-1.5 text-[10px] uppercase tracking-wide text-slate-400 border-b border-slate-800">Destination projection</div>
                  <div className="px-2 py-2 space-y-1 text-slate-400">
                    <div className="flex items-center gap-2">
                      <span className="text-slate-300">Edge:</span>
                      <span className="text-slate-200">{route.destinationPointProjection.edge?.name ?? 'N/A'}</span>
                      <span className="ml-auto text-slate-500">#{route.destinationPointProjection.edge?.id}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-slate-300">Distance from start:</span>
                      <span className="text-sky-300">{fmt(route.destinationPointProjection.distanceFromStart)} m</span>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div className="text-slate-400">Original: ({fmt(route.destinationPointProjection.originalPoint?.x)}, {fmt(route.destinationPointProjection.originalPoint?.y)})</div>
                      <div className="text-slate-400">Projection: ({fmt(route.destinationPointProjection.projectionPoint?.x)}, {fmt(route.destinationPointProjection.projectionPoint?.y)})</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Steps section */}
              <div className="pt-1">
                <div className="text-xs font-semibold text-slate-400 mb-1">Steps ({route.route.length})</div>
                {route.route.length ? (
                  <ol className="space-y-1">
                    {route.route.map((s, idx) => (
                      <li key={idx} className="rounded border border-slate-800/80 bg-slate-900/20">
                        <div className="px-2 py-1.5 flex items-center gap-2 text-slate-300">
                          <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-200">{idx + 1}</span>
                          <span className="truncate">
                            {s.instruction || `Move to (${fmt(s.x)}, ${fmt(s.y)})`}
                          </span>
                          <span className="ml-auto text-[10px] text-slate-400">speed {fmt(s.speed)}</span>
                        </div>
                        <div className="px-2 pb-1 text-[10px] text-slate-500">Point: ({fmt(s.x)}, {fmt(s.y)})</div>
                      </li>
                    ))}
                  </ol>
                ) : (
                  <div className="text-[11px] text-slate-500">No steps in route.</div>
                )}
              </div>
            </div>
          ) : (
            <div className="text-[11px] text-slate-500">Route not loaded.</div>
          )}
        </div>
      </div>
    </div>
  );
}
