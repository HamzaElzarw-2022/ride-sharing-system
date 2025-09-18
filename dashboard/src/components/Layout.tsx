import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, ListOrdered, MapPinned, Car } from 'lucide-react';
import Sidebar, { type SidebarActiveTab } from './Sidebar';
import { MonitoringProvider } from '../context/MonitoringContext';

const navItems = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const [sidebarTab, setSidebarTab] = useState<SidebarActiveTab>('events');

  return (
    <MonitoringProvider>
      <div className="min-h-screen bg-slate-900 text-slate-100 flex">

        {/* Left icon-only nav */}
        <aside className="w-16 border-r border-slate-800 bg-slate-950 flex flex-col items-center gap-4">
          <div className="py-3 px-3 border-b border-slate-800 flex items-center gap-2">
            <div className="text-xl font-bold h-10 w-10 grid place-items-center rounded bg-gradient-to-br from-indigo-500 to-cyan-500">
              RS
            </div>
          </div>

          <nav className="flex flex-col items-center gap-3">
            {navItems.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                className={`p-2 rounded-lg transition-colors border ${location.pathname !== item.href ? 'bg-slate-800 text-white border-slate-600' : 'bg-transparent text-slate-300 border-slate-700 hover:bg-slate-800/50'}`}
                title={item.name}
              >
                <item.icon size={22} />
              </Link>
            ))}
          </nav>
          <button
            className={`p-2 rounded-lg transition-colors border ${sidebarTab === 'events' ? 'bg-slate-800 text-white border-slate-600' : 'bg-transparent text-slate-300 border-slate-700 hover:bg-slate-800/50'}`}
            onClick={() => setSidebarTab('events')}
            title="Show Events"
          >
            <ListOrdered size={22} />
          </button>
          <button
            className={`p-2 rounded-lg transition-colors border ${sidebarTab === 'trips' ? 'bg-slate-800 text-white border-slate-600' : 'bg-transparent text-slate-300 border-slate-700 hover:bg-slate-800/50'}`}
            onClick={() => setSidebarTab('trips')}
            title="Show Trips"
          >
            <MapPinned size={22} />
          </button>
          <button
            className={`p-2 rounded-lg transition-colors border ${sidebarTab === 'drivers' ? 'bg-slate-800 text-white border-slate-600' : 'bg-transparent text-slate-300 border-slate-700 hover:bg-slate-800/50'}`}
            onClick={() => setSidebarTab('drivers')}
            title="Show Drivers"
          >
            <Car size={22} />
          </button>
        </aside>

        {/* Secondary sidebar */}
        <Sidebar activeTab={sidebarTab} onChangeTab={setSidebarTab} />

        {/* Main content */}
        <main className="flex-1 flex flex-col">
          {/*/!* Top bar within main area *!/*/}
          {/*<div className="h-14 border-b border-slate-800 bg-slate-900/70 backdrop-blur-sm flex items-center px-4">*/}
          {/*  <div className="text-sm text-slate-300">Ride Sharing Dashboard</div>*/}
          {/*</div>*/}
          <div className="flex-1 p-4 flex">

            <div className="w-full h-full rounded-xl overflow-hidden border border-slate-800 bg-slate-950">
              {children}
            </div>
          </div>
        </main>
      </div>
    </MonitoringProvider>
  );
}