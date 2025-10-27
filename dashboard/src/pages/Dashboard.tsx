import MapCanvas from '../components/MapCanvas';
import Sidebar from '../components/Sidebar';

export default function Dashboard() {
  return (
    <div className="flex w-full h-full">
      <Sidebar />
      <div className="flex-1 p-4 flex">
        <div className="flex-1 h-full rounded-xl overflow-hidden border border-slate-800">
          <MapCanvas />
        </div>
      </div>
      
    </div>
  );
}