
type PanelState = 'initial' | 'route_selected' | 'matching' | 'in_progress';

interface RiderPanelProps {
  state: PanelState;
  onConfirm: () => void;
}

export default function RiderPanel({ state, onConfirm }: RiderPanelProps) {
  return (
    <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl shadow-lg p-4 text-slate-800">
      {state === 'initial' && (
        <div>
          <h2 className="font-bold text-lg">Where to?</h2>
          <p className="text-sm text-slate-500">Select your start and destination on the map.</p>
        </div>
      )}
      {state === 'route_selected' && (
        <div>
          <h2 className="font-bold text-lg">Confirm your trip</h2>
          <p className="text-sm text-slate-500">Ready to go?</p>
          <button
            onClick={onConfirm}
            className="w-full bg-indigo-500 text-white py-3 rounded-lg font-semibold mt-4"
          >
            Confirm and Request
          </button>
        </div>
      )}
      {state === 'matching' && (
        <div className="flex flex-col items-center">
          <h2 className="font-bold text-lg">Finding your driver...</h2>
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500 my-4"></div>
        </div>
      )}
      {state === 'in_progress' && (
        <div>
          <h2 className="font-bold text-lg">Trip in Progress</h2>
          <p className="text-sm text-slate-500">Your driver is on the way.</p>
        </div>
      )}
    </div>
  );
}
