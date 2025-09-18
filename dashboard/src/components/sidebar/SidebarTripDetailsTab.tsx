import React, { useMemo } from 'react';
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
        No trip selected.
      </div>
    );
  }

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
      <div className="p-3 space-y-3 overflow-auto">
        <div className="text-[12px] text-slate-300 flex items-center gap-2"><User size={14} className="text-yellow-300"/> Rider #{trip.riderId}</div>
        {trip.driverId != null && (
          <div className="text-[12px] text-slate-300 flex items-center gap-2"><Car size={14} className="text-emerald-300"/> Driver #{trip.driverId}</div>
        )}
        <div className="text-[12px] text-slate-400 flex items-center gap-2"><MapPin size={14} className="text-sky-300"/> Start: ({trip.startLongitude.toFixed(2)}, {trip.startLatitude.toFixed(2)})</div>
        <div className="text-[12px] text-slate-400 flex items-center gap-2"><Flag size={14} className="text-rose-300"/> End: ({trip.endLongitude.toFixed(2)}, {trip.endLatitude.toFixed(2)})</div>
        <div className="pt-2 border-t border-slate-800">
          <div className="text-xs font-semibold text-slate-400 mb-2 flex items-center gap-2"><Map size={14}/> Route</div>
          {route ? (
            <div className="text-[11px] text-slate-400 space-y-1">
              <div>Steps: {route.route.length}</div>
              <div>From projection to projection with dashed connectors rendered on map.</div>
            </div>
          ) : (
            <div className="text-[11px] text-slate-500">Route not loaded.</div>
          )}
        </div>
      </div>
    </div>
  );
}
