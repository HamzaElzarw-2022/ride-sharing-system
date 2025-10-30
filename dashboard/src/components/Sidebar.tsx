import { useMemo, useState } from 'react';
import { ListOrdered, MapPinned, Car } from 'lucide-react';
import SidebarEventsTab from './sidebar/SidebarEventsTab';
import SidebarTripsTab from './sidebar/SidebarTripsTab';
import SidebarTripDetailsTab from './sidebar/SidebarTripDetailsTab';
import SidebarDriversTab from './sidebar/SidebarDriversTab';
import { useMonitoring } from '../context/MonitoringContext';

export type SidebarActiveTab = 'events' | 'trips' | 'tripDetails' | 'drivers';

const TABS: { id: SidebarActiveTab, label: string, icon: React.ElementType }[] = [
    { id: 'events', label: 'Events', icon: ListOrdered },
    { id: 'trips', label: 'Trips', icon: MapPinned },
    { id: 'drivers', label: 'Drivers', icon: Car },
];

export default function Sidebar() {
    const [activeTab, setActiveTab] = useState<SidebarActiveTab>('events');
    const { setSelectedTripId, trips, drivers } = useMonitoring();

    function handleOpenTrip(tripId: number) {
        setSelectedTripId(tripId);
        setActiveTab('tripDetails');
    }

    const tripsCount = trips.length;

    const { idleDriversCount, totalDriversCount } = useMemo(() => {
        const totalDrivers = Object.keys(drivers).length;
        const busyDriverIds = new Set(trips.filter(t => t.status === 'STARTED' || t.status === 'PICKING_UP').map(t => t.driverId));
        const idleDrivers = totalDrivers - busyDriverIds.size;
        return { idleDriversCount: idleDrivers, totalDriversCount: totalDrivers };
    }, [drivers, trips]);

    const currentTab = TABS.find(t => t.id === activeTab);

    return (
        <aside className="w-80 border-r border-slate-800 bg-slate-900/60 backdrop-blur-sm hidden md:flex md:flex-col max-h-screen">
            <div className="px-3 py-3 border-b bg-slate-950 border-slate-800 flex items-center gap-2">
                <h1 className="text-md h-10 font-semibold text-slate-300 pt-2.5">
                    Ride Sharing Dashboard
                </h1>
            </div>

            {/* Tabs */}
            <div className="p-2 border-b border-slate-800 flex items-center gap-2">
                {TABS.map(tab => (
                    <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id)}
                        className={`flex-1 flex items-center justify-center gap-2 p-2 rounded-md text-sm transition-colors ${activeTab === tab.id ? 'bg-slate-800 text-white' : 'text-slate-400 hover:bg-slate-800/50'}`}
                        title={tab.label}
                    >
                        <tab.icon size={18} />
                        <span>{tab.label}</span>
                    </button>
                ))}
            </div>

            <div className="p-4 h-10 border-b border-slate-800 flex items-center gap-2">
                <h2 className="text-sm font-semibold text-slate-300">
                    {activeTab === 'tripDetails' ? 'Trip Details' : currentTab?.label}
                </h2>
                {activeTab === 'trips' && <span className="text-sm text-slate-400">({tripsCount})</span>}
                {activeTab === 'drivers' && <span className="text-sm text-slate-400">({idleDriversCount} idle / {totalDriversCount} total)</span>}
                {activeTab === 'tripDetails' && (
                    <button onClick={() => setActiveTab('trips')} className="ml-auto text-xs text-slate-400 hover:text-white">Back to trips</button>
                )}
            </div>

            <div className="overflow-y-auto flex-grow custom-scrollbar">
                {activeTab === 'events' && <SidebarEventsTab />}
                {activeTab === 'trips' && <SidebarTripsTab onOpenTrip={handleOpenTrip} />}
                {activeTab === 'drivers' && <SidebarDriversTab />}
                {activeTab === 'tripDetails' && <SidebarTripDetailsTab onBack={() => setActiveTab('trips')} />}
            </div>
        </aside>
    );
}
