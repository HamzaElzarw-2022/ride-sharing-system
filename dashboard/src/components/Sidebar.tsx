import SidebarEventsTab from './sidebar/SidebarEventsTab';
import SidebarTripsTab from './sidebar/SidebarTripsTab';
import SidebarTripDetailsTab from './sidebar/SidebarTripDetailsTab';
import { useMonitoring } from '../context/MonitoringContext';

export type SidebarActiveTab = 'events' | 'trips' | 'tripDetails';

export default function Sidebar({ activeTab, onChangeTab }: { activeTab: SidebarActiveTab; onChangeTab: (tab: SidebarActiveTab) => void; }) {
  const { setSelectedTripId } = useMonitoring();

  function handleOpenTrip(tripId: number) {
    setSelectedTripId(tripId);
    onChangeTab('tripDetails');
  }

  return (
    <aside className="w-80 border-r border-slate-800 bg-slate-900/60 backdrop-blur-sm hidden md:flex md:flex-col">
      <div className="px-3 py-3 border-b bg-slate-950 border-slate-800 flex items-center gap-2">
        <h1 className="text-md h-10 font-semibold text-slate-300 pt-2.5">
          Ride Sharing Dashboard
        </h1>
      </div>
      <div className="p-4 h-10 border-b border-slate-800 flex items-center gap-2">
        <h2 className="text-sm font-semibold text-slate-300">
          {activeTab === 'events' ? 'Live Events' : activeTab === 'trips' ? 'Trips' : 'Trip Details'}
        </h2>
        {activeTab === 'tripDetails' && (
          <button onClick={() => onChangeTab('trips')} className="ml-auto text-xs text-slate-400 hover:text-white">Back to trips</button>
        )}
      </div>
      {activeTab === 'events' && <SidebarEventsTab />}
      {activeTab === 'trips' && <SidebarTripsTab onOpenTrip={handleOpenTrip} />}
      {activeTab === 'tripDetails' && <SidebarTripDetailsTab onBack={() => onChangeTab('trips')} />}
    </aside>
  );
}
