import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { LayoutDashboard, Smartphone } from 'lucide-react';
import { MonitoringProvider } from '../context/MonitoringContext';
import githubLogo from '../assets/github-mark-white.svg';

const navItems = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Rider App', href: '/rider', icon: Smartphone },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();

  return (
    <MonitoringProvider>
      <div className="min-h-screen bg-slate-900 text-slate-100 flex">

        {/* Left icon-only nav */}
        <aside className="w-16 border-r border-slate-800 bg-slate-950 flex flex-col items-center">
          <div className="flex flex-col items-center gap-4 flex-grow">
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
                  className={`p-2 rounded-lg transition-colors border ${location.pathname === item.href ? 'bg-slate-800 text-white border-slate-600' : 'bg-transparent text-slate-300 border-slate-700 hover:bg-slate-800/50'}`}
                  title={item.name}
                >
                  <item.icon size={22} />
                </Link>
              ))}
            </nav>
          </div>

          <div className="p-4">
            <a href="https://github.com/HamzaElzarw-2022/ride-sharing-system" target="_blank" rel="noopener noreferrer" title="View on GitHub">
              <img src={githubLogo} alt="GitHub" className="w-8 h-8 opacity-80 hover:opacity-100 transition-opacity" />
            </a>
          </div>
        </aside>

        {/* Main content */}
        <main className="flex-1 flex flex-col">
          <div className="w-full h-full bg-slate-950">
            {children}
          </div>
        </main>
      </div>
    </MonitoringProvider>
  );
}