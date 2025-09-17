import MapCanvas from '../components/MapCanvas';

export default function Dashboard() {
  return (
    <div className="w-full h-[calc(100vh-3.5rem-2rem)] md:h-[calc(100vh-3.5rem-2rem)]">
      <MapCanvas />
    </div>
  );
}