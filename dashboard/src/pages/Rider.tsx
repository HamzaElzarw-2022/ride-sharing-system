import { useState } from 'react';
import MapContainer from '../components/MapContainer';
import { useAuth } from '../context/AuthContext';
import AuthView from '../components/AuthView';
import RiderPanel from '../components/RiderPanel';
import type { Point, RouteStep } from '../services/routeService';
import { fetchRoute } from '../services/routeService';
import { requestTrip } from '../services/tripService';
import RouteLayer from '../components/map/RouteLayer';

type RiderState = 'initial' | 'selecting_start' | 'selecting_end' | 'route_selected' | 'matching' | 'in_progress' | 'completed';

export default function Rider() {
  const { auth } = useAuth();
  const [state, setState] = useState<RiderState>('initial');
  const [startPoint, setStartPoint] = useState<Point | null>(null);
  const [endPoint, setEndPoint] = useState<Point | null>(null);
  const [route, setRoute] = useState<RouteStep[]>([]);

  const handleMapClick = async (point: Point) => {
    if (state === 'initial' || state === 'selecting_start') {
      setStartPoint(point);
      setState('selecting_end');
    } else if (state === 'selecting_end') {
      setEndPoint(point);
      if (startPoint) {
        const routeResponse = await fetchRoute({ startPoint, destinationPoint: point });
        setRoute(routeResponse.route);
        setState('route_selected');
      }
    }
  };

  const handleConfirmTrip = async () => {
    if (startPoint && endPoint) {
      setState('matching');
      try {
        const trip = await requestTrip({ start: startPoint, end: endPoint });
        // Connect to websocket here
      } catch (error) {
        console.error("Failed to request trip", error);
        setState('route_selected'); // Or show an error state
      }
    }
  };

  if (!auth) {
    return (
      <div className="w-full h-full flex items-center justify-center bg-slate-950">
        <div className="w-full h-full md:w-[420px] md:h-[850px] md:rounded-[40px] md:border-[10px] md:border-black md:shadow-2xl overflow-hidden flex flex-col text-slate-800">
          <AuthView />
        </div>
      </div>
    );
  }

  return (
    <div className="w-full h-full flex items-center justify-center bg-slate-950">
      <div className="w-full h-full md:w-[420px] md:h-[850px] md:rounded-[40px] md:border-[10px] md:border-black md:shadow-2xl overflow-hidden flex flex-col text-slate-800 relative">
        <MapContainer onMapClick={handleMapClick}>
          <RouteLayer route={route} start={startPoint} end={endPoint} />
        </MapContainer>
        <RiderPanel
          state={state === 'initial' || state === 'selecting_start' || state === 'selecting_end' ? 'initial' : state}
          onConfirm={handleConfirmTrip}
        />
      </div>
    </div>
  );
}
