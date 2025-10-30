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
import DriverLayer from '../components/map/DriverLayer';
import Guide from '../components/Guide';

type DriverLocation = Point & { degree: number };

export default function Rider() {
  const { auth } = useAuth();
  const [state, setState] = useState<RiderState>('initial');
  const [startPoint, setStartPoint] = useState<Point | null>(null);
  const [endPoint, setEndPoint] = useState<Point | null>(null);
  const [route, setRoute] = useState<RouteStep[]>([]);
  const [isSelecting, setIsSelecting] = useState<'start' | 'end' | null>(null);
  const [tripId, setTripId] = useState<number | null>(null);
  const [driverLocation, setDriverLocation] = useState<DriverLocation | null>(null);
  const [isGuideOpen, setIsGuideOpen] = useState(true);
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
        setDriverLocation({ x: msg.payload.x, y: msg.payload.y, degree: msg.payload.degree });
        break;
    }
  };

  const getPointName = (point: Point | null, type: 'start' | 'end') => {
    if (isSelecting === type) return 'Select on map...';
    if (!point) return '';
    return `(${point.x.toFixed(2)}, ${point.y.toFixed(2)})`;
  };

  const showBackButton = (isSelecting !== null) || (state !== 'initial' && state !== 'matching' && state !== 'started' && state !== 'picking_up');

  return (
    <div className="w-full h-full flex items-center justify-center bg-slate-950">
      <div className="w-full h-full md:w-[420px] md:h-[850px] md:rounded-[40px] md:border-[10px] md:border-black md:shadow-2xl overflow-hidden flex flex-col text-slate-800 relative">
        {!auth ? (
          <AuthView />
        ) : (
          <>
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
              <DriverLayer position={driverLocation} />
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
          </>
        )}
      </div>
      <div className="absolute top-5 right-5 md:w-1/3 w-9/12">
        <Guide title="Rider App Simulation Guide" isGuideOpen={isGuideOpen} setIsGuideOpen={setIsGuideOpen}>
          <p>Welcome to the interactive ride-sharing simulation. This guide will walk you through the features of the Rider App.</p>
          <ol className="list-decimal list-inside space-y-2 mt-4">
            <li>
              <strong>📍 Set Your Route:</strong> Use the "Set" buttons to select your start and destination points. Click directly on the map to place your markers.
            </li>
            <li>
              <strong>👍 Confirm Your Trip:</strong> Once both points are set, a route will be calculated and displayed. Click "Confirm and Request" to initiate a trip with a simulated driver.
            </li>
            <li>
              <strong>🤖 Driver Matching:</strong> The system will then search for an available driver in the simulation. If a driver accepts, your trip will begin. Otherwise, you'll be notified that no drivers were found, and you can try again.
            </li>
            <li>
              <strong>🗺️ Real-Time Tracking:</strong> Once matched, you can track your driver's location in real-time on the map as they navigate to your pickup point and then to your destination.
            </li>
            <li>
              <strong>🚦 Live Status Updates:</strong> The panel at the bottom of the screen provides live updates on your trip's status, from "Picking up" to "Trip started" and finally "Completed".
            </li>
          </ol>
          <p className="mt-4">
            For a comprehensive overview of the entire simulation, including all active drivers and trips, please visit the <strong>Dashboard</strong> page.
          </p>
        </Guide>
      </div>
    </div>
  );
}

