import { Search, MapPin, ArrowRight, Locate } from 'lucide-react';
import type { RiderState } from '../types';

interface RiderPanelProps {
  state: RiderState;
  onConfirm: () => void;
  onSetDestination: () => void;
  onSetStart: () => void;
  destination: string;
  start: string;
  isSelecting: 'start' | 'end' | null;
}

export default function RiderPanel({ state, onConfirm, onSetDestination, onSetStart, destination, start, isSelecting }: RiderPanelProps) {

  const renderInputs = () => (
    <div className="space-y-2">
      <div
        className={`flex items-center bg-slate-100 rounded-lg px-3 py-2 cursor-pointer transition-all ${isSelecting === 'end' ? 'ring-2 ring-indigo-500' : 'ring-1 ring-transparent'}`}
        onClick={onSetDestination}
      >
        <MapPin size={20} className="text-slate-400" />
        <input
          type="text"
          placeholder="Choose destination"
          className="bg-transparent ml-2 w-full outline-none cursor-pointer"
          value={destination}
          readOnly
        />
        <button
          onClick={(e) => { e.stopPropagation(); onSetDestination(); }}
          className="text-indigo-500 font-semibold text-sm"
        >
          Set
        </button>
      </div>
      {(destination || start || state === 'route_selected') && (
        <div
          className={`flex items-center bg-slate-100 rounded-lg px-3 py-2 cursor-pointer transition-all ${isSelecting === 'start' ? 'ring-2 ring-indigo-500' : 'ring-1 ring-transparent'}`}
          onClick={onSetStart}
        >
          <Locate size={20} className="text-slate-400" />
          <input
            type="text"
            placeholder="Choose start point"
            className="bg-transparent ml-2 w-full outline-none cursor-pointer"
            value={start}
            readOnly
          />
          <button
            onClick={(e) => { e.stopPropagation(); onSetStart(); }}
            className="text-indigo-500 font-semibold text-sm"
          >
            Set
          </button>
        </div>
      )}
    </div>
  );

  const renderRouteSelected = () => (
    <div>
      <div className="flex items-center justify-between mb-2">
        <h2 className="font-bold text-lg">Confirm your trip</h2>
        <div className="flex items-center gap-2 text-sm">
          <div className="flex items-center gap-1">
            <MapPin size={16} className="text-green-500" />
            <span>Start</span>
          </div>
          <ArrowRight size={16} className="text-slate-400" />
          <div className="flex items-center gap-1">
            <MapPin size={16} className="text-red-500" />
            <span>End</span>
          </div>
        </div>
      </div>
      {renderInputs()}
      <button
        onClick={onConfirm}
        className="w-full bg-indigo-500 text-white py-3 rounded-lg font-semibold mt-4"
      >
        Confirm and Request
      </button>
    </div>
  );

  const renderMatching = () => (
    <div className="flex flex-col items-center">
      <h2 className="font-bold text-lg">Finding your driver...</h2>
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500 my-4"></div>
    </div>
  );

  const renderInProgress = () => (
    <div>
      <h2 className="font-bold text-lg">Trip in Progress</h2>
      <p className="text-sm text-slate-500">Your driver is on the way.</p>
    </div>
  );

  const renderContent = () => {
    switch (state) {
      case 'route_selected':
        return renderRouteSelected();
      case 'matching':
        return renderMatching();
      case 'in_progress':
        return renderInProgress();
      case 'initial':
      case 'selecting_start':
      case 'selecting_end':
      default:
        return (
          <div>
            <h2 className="font-bold text-lg mb-2">Where to?</h2>
            {renderInputs()}
          </div>
        );
    }
  };

  return (
    <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl shadow-lg p-4 text-slate-800 z-20">
      {renderContent()}
    </div>
  );
}