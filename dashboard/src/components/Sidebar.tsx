import React, { useEffect, useMemo, useState } from 'react';
import { Bell, CheckCircle2, Flag, Handshake, MapPin } from 'lucide-react';
import { connectMonitoring, type MonitoringMessage } from '../services/monitoringService';

type EventItem = {
  id: string;
  type: 'trip.created' | 'trip.matched' | 'trip.started' | 'trip.ended';
  ts: number;
  payload: any;
};

function formatTime(ts: number) {
  try {
    const d = new Date(ts);
    return d.toLocaleTimeString(undefined, { hour12: false });
  } catch {
    return String(ts);
  }
}

function eventIcon(type: EventItem['type']) {
  switch (type) {
    case 'trip.created': return <Bell size={14} className="text-sky-300"/>;
    case 'trip.matched': return <Handshake size={14} className="text-emerald-300"/>;
    case 'trip.started': return <MapPin size={14} className="text-amber-300"/>;
    case 'trip.ended': return <Flag size={14} className="text-rose-300"/>;
  }
}

function typeLabel(type: EventItem['type']) {
  switch (type) {
    case 'trip.created': return 'Trip created';
    case 'trip.matched': return 'Trip matched';
    case 'trip.started': return 'Trip started';
    case 'trip.ended': return 'Trip ended';
  }
}

export default function Sidebar() {
  const [events, setEvents] = useState<EventItem[]>([]);

  useEffect(() => {
    const ws = connectMonitoring((msg: MonitoringMessage) => {
      if (msg.type === 'driver.locations') return; // exclude driver location events
      // Only keep trip.* messages
      if (msg.type.startsWith('trip.')) {
        const id = `${msg.type}:${(msg as any).payload?.tripId ?? msg.ts}`;
        const item: EventItem = { id, type: msg.type as EventItem['type'], ts: msg.ts, payload: (msg as any).payload };
        setEvents(prev => {
          const next = [item, ...prev];
          if (next.length > 15) next.length = 15;
          return next;
        });
      }
    });
    return () => { try { ws.close(); } catch {} };
  }, []);

  return (
    <aside className="w-72 border-r border-slate-800 bg-slate-900/60 backdrop-blur-sm hidden md:flex md:flex-col">
      <div className="p-4 border-b border-slate-800">
        <h2 className="text-sm font-semibold text-slate-300">Live Events</h2>
        <p className="text-xs text-slate-500">Latest 15 trip events</p>
      </div>
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
    </aside>
  );
}
