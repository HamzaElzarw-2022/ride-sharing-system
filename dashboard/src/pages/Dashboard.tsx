import MapContainer from '../components/MapContainer';
import Sidebar from '../components/Sidebar';
import { useMonitoring } from '../context/MonitoringContext';
import TripLayer from '../components/map/TripLayer';
import DriversLayer from '../components/map/DriversLayer';
import BaseMap from '../components/map/BaseMap';

export default function Dashboard() {
  const { trips, drivers, routes } = useMonitoring();

  return (
    <div className="flex w-full h-full">
      <Sidebar />
      <div className="flex-1 h-full overflow-hidden">
        <MapContainer>
          <BaseMap />
          <TripLayer trips={trips} routes={routes} /> 
          <DriversLayer drivers={drivers} trips={trips} />
        </MapContainer>
      </div>
    </div>
  );
}