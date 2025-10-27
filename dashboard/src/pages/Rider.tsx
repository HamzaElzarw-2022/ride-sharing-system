import MapCanvas from '../components/MapCanvas';

export default function Rider() {
  return (
    <div className="w-full h-full flex flex-col">
      <div className="flex-1">
        <MapCanvas />
      </div>
      <div className="p-4 border-t border-gray-200">
        <button className="w-full bg-indigo-500 text-white py-3 rounded-lg font-semibold">
          Request Trip
        </button>
      </div>
    </div>
  );
}
