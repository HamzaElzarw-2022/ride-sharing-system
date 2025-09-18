import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Map as MapIcon, Car, Route } from 'lucide-react';
import Sidebar from './Sidebar';

const navItems = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Map', href: '/', icon: MapIcon },
  { name: 'Drivers', href: '/', icon: Car },
  { name: 'Routes', href: '/', icon: Route },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex">
      {/* Left icon-only nav */}
      <aside className="w-16 border-r border-slate-800 bg-slate-950 flex flex-col items-center py-4 gap-4">
        <div className="text-xl font-bold h-10 w-10 grid place-items-center rounded bg-gradient-to-br from-indigo-500 to-cyan-500">
          RS
        </div>
        <nav className="flex flex-col items-center gap-3 mt-2">
          {navItems.map((item) => (
            <Link
              key={item.name}
              to={item.href}
              className={`p-2 rounded-lg transition-colors ${location.pathname === item.href ? 'bg-slate-800 text-white' : 'text-slate-400 hover:text-white hover:bg-slate-800'}`}
              title={item.name}
            >
              <item.icon size={22} />
            </Link>
          ))}
        </nav>
      </aside>

      {/* Secondary sidebar */}
      <Sidebar />

      {/* Main content */}
      <main className="flex-1 flex flex-col">
        {/* Top bar within main area */}
        <div className="h-14 border-b border-slate-800 bg-slate-900/70 backdrop-blur-sm flex items-center px-4">
          <div className="text-sm text-slate-300">Ride Sharing Dashboard</div>
        </div>
        <div className="flex-1 p-4">
          <div className="w-full h-full rounded-xl overflow-hidden border border-slate-800 bg-slate-950">
            {children}
          </div>
        </div>
      </main>
    </div>
  );
}