import { Bell, Flag, Handshake, MapPin } from 'lucide-react';
import { useMonitoring } from '../../context/MonitoringContext';

function formatTime(ts: number) {
  try { const d = new Date(ts); return d.toLocaleTimeString(undefined, { hour12: false }); } catch { return String(ts); }
}

function eventIcon(type: 'trip.created' | 'trip.matched' | 'trip.started' | 'trip.ended') {
  switch (type) {
    case 'trip.created': return <Bell size={14} className="text-sky-300"/>;
    case 'trip.matched': return <Handshake size={14} className="text-emerald-300"/>;
    case 'trip.started': return <MapPin size={14} className="text-amber-300"/>;
    case 'trip.ended': return <Flag size={14} className="text-rose-300"/>;
  }
}

function typeLabel(type: 'trip.created' | 'trip.matched' | 'trip.started' | 'trip.ended') {
  switch (type) {
    case 'trip.created': return 'Trip created';
    case 'trip.matched': return 'Trip matched';
    case 'trip.started': return 'Trip started';
    case 'trip.ended': return 'Trip ended';
  }
}

export default function SidebarEventsTab() {
  const { events } = useMonitoring();
  return (
    <div className="p-3 overflow-auto space-y-2">
      {events.length === 0 && (
        <div className="p-3 rounded-lg bg-slate-800/40 border border-slate-700/40 text-xs text-slate-400">No events yet</div>
      )}
      <ul className="space-y-2">
        {events.map(ev => (
          <li key={ev.id} className="p-3 rounded-lg bg-slate-800/50 border border-slate-700/50">
            <div className="flex items-center gap-2">
              <div className="shrink-0">{eventIcon(ev.type)}</div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-medium text-slate-200 truncate">{typeLabel(ev.type)}</span>
                  <span className="text-[10px] text-slate-500 tabular-nums">{formatTime(ev.ts)}</span>
                </div>
                <div className="mt-1 text-[11px] text-slate-400">
                  {ev.payload?.tripId !== undefined && (
                    <span>Trip #{String(ev.payload.tripId)}</span>
                  )}
                  {ev.type === 'trip.matched' && ev.payload?.driverId !== undefined && (
                    <span className="ml-2 text-slate-500">Driver #{String(ev.payload.driverId)}</span>
                  )}
                  {ev.type === 'trip.created' && ev.payload?.riderId !== undefined && (
                    <span className="ml-2 text-slate-500">Rider #{String(ev.payload.riderId)}</span>
                  )}
                </div>
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
