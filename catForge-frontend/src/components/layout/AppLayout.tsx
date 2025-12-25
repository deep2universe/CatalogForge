import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';

const pageTitles: Record<string, string> = {
  '/': 'Dashboard',
  '/skills': 'Skill Explorer',
  '/prompts': 'Prompt Explorer',
  '/wizard': 'Katalog erstellen',
};

export function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const title = location.pathname.startsWith('/preview')
    ? 'Katalog-Vorschau'
    : pageTitles[location.pathname] || 'CatalogForge';

  return (
    <div className="flex min-h-screen bg-neutral-50">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex-1 flex flex-col min-w-0">
        <Header title={title} onMenuClick={() => setSidebarOpen(true)} />
        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
