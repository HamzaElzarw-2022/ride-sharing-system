import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard } from 'lucide-react';

const navigation = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
];

export default function Layout({ children }) {

  return (
    <div className="min-h-screen bg-gray-50">
     
    </div>
  );
}