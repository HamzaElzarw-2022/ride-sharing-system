import { useEffect, useState, useRef } from 'react';
import MapContainer from '../components/MapContainer';
import { useAuth } from '../context/AuthContext';
import AuthView from '../components/AuthView';
import RiderPanel from '../components/RiderPanel';
import type { Point, RouteStep } from '../services/routeService';
import { fetchRoute } from '../services/routeService';
import { requestTrip } from '../services/tripService';
import RouteLayer from '../components/map/RouteLayer';
import BaseMap from '../components/map/BaseMap';
import type { RiderState } from '../types';
import { ArrowLeft } from 'lucide-react';
import { connectTrip, type TripMessage } from '../services/tripWebSocketService';

export default function Rider() {
  const { auth } = useAuth();
  const [state, setState] = useState<RiderState>('initial');
  const [startPoint, setStartPoint] = useState<Point | null>(null);
  const [endPoint, setEndPoint] = useState<Point | null>(null);
  const [route, setRoute] = useState<RouteStep[]>([]);
  const [isSelecting, setIsSelecting] = useState<'start' | 'end' | null>(null);
  const [tripId, setTripId] = useState<number | null>(null);
  const [driverLocation, setDriverLocation] = useState<Point | null>(null);
  const ws = useRef<WebSocket | null>(null);

  const handleBack = () => {
    setState('initial');
    setStartPoint(null);
    setEndPoint(null);
    setRoute([]);
    setIsSelecting(null);
    setTripId(null);
    setDriverLocation(null);
    if (ws.current) {
      ws.current.close();
      ws.current = null;
    }
  };

  const handleMapClick = async (point: Point) => {
    if (isSelecting === 'start') {
      setStartPoint(point);
    } else if (isSelecting === 'end') {
      setEndPoint(point);
    }
    setIsSelecting(null);
  };

  useEffect(() => {
    if (startPoint && endPoint) {
      fetchRoute({ startPoint, destinationPoint: endPoint })
        .then(routeResponse => {
          setRoute(routeResponse.route);
          setState('route_selected');
        })
        .catch(error => console.error("Failed to fetch route", error));
    }
  }, [startPoint, endPoint]);

  const handleConfirmTrip = async () => {
    if (startPoint && endPoint) {
      setState('matching');
      try {
        const response = await requestTrip({ start: startPoint, end: endPoint });
        console.log("Trip requested successfully: " + response.id);
        setTripId(response.id);
      } catch (error) {
        console.error("Failed to request trip", error);
        setState('route_selected'); // Or show an error state
      }
    }
  };

  useEffect(() => {
    if (tripId) {
      ws.current = connectTrip(tripId, handleTripMessage);
    }
    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, [tripId]);

  const handleTripMessage = (msg: TripMessage) => {
    console.log("Received trip message", msg.type);
    switch (msg.type) {
      case 'trip.matched':
        setState('picking_up');
        break;
      case 'trip.started':
        setState('started');
        break;
      case 'trip.ended':
        setState(currentState => {
          if (currentState === 'matching') {
            return 'no_driver_found';
          } else {
            return 'completed';
          }
        });
        if (ws.current) {
          ws.current.close();
        }
        break;
      case 'driver.location':
        setState(currentState => {
          if (currentState === 'matching') {
            return 'picking_up';
          } else {
            return currentState;
          }
        });
        setDriverLocation({ x: msg.payload.x, y: msg.payload.y });
        break;
    }
  };

  const getPointName = (point: Point | null, type: 'start' | 'end') => {
    if (isSelecting === type) return 'Select on map...';
    if (!point) return '';
    return `(${point.x.toFixed(2)}, ${point.y.toFixed(2)})`;
  };

  const showBackButton = (isSelecting !== null) || (state !== 'initial' && state !== 'matching' && state !== 'started' && state !== 'picking_up');

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
        {showBackButton && (
          <button
            onClick={handleBack}
            className="absolute top-5 left-5 z-20 bg-white rounded-full p-2 shadow-md hover:bg-slate-100 transition-colors"
          >
            <ArrowLeft size={20} className="text-slate-800" />
          </button>
        )}
        <MapContainer onMapClick={isSelecting ? handleMapClick : undefined}>
          <BaseMap />
          <RouteLayer route={route} start={startPoint} end={endPoint} />
          {/* {driverLocation} */}
        </MapContainer>
        <RiderPanel
          state={state}
          onConfirm={handleConfirmTrip}
          onSetDestination={() => setIsSelecting('end')}
          onSetStart={() => setIsSelecting('start')}
          destination={getPointName(endPoint, 'end')}
          start={getPointName(startPoint, 'start')}
          isSelecting={isSelecting}
        />
      </div>
    </div>
  );
}

