import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Sparkles, MessageSquare, Wand2 } from 'lucide-react';
import { cn } from '@/utils/cn';

const navigationItems = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/skills', label: 'Skill Explorer', icon: Sparkles },
  { path: '/prompts', label: 'Prompt Explorer', icon: MessageSquare },
];

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 md:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed top-0 left-0 z-50 h-full w-64 bg-sidebar border-r border-neutral-200',
          'transform transition-transform duration-200 ease-in-out',
          'md:translate-x-0 md:static md:z-0',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex flex-col h-full">
          {/* Logo */}
          <div className="px-6 py-5 border-b border-neutral-200">
            <h1 className="text-xl font-semibold text-neutral-800">
              🏭 CatalogForge
            </h1>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-4">
            <ul className="space-y-1">
              {navigationItems.map((item) => (
                <li key={item.path}>
                  <NavLink
                    to={item.path}
                    onClick={onClose}
                    className={({ isActive }) =>
                      cn(
                        'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                        isActive
                          ? 'bg-pastel-blue text-neutral-800'
                          : 'text-neutral-600 hover:bg-neutral-100 hover:text-neutral-800'
                      )
                    }
                  >
                    <item.icon className="h-5 w-5" />
                    {item.label}
                  </NavLink>
                </li>
              ))}
            </ul>

            {/* Primary CTA */}
            <div className="mt-6 pt-6 border-t border-neutral-200">
              <NavLink
                to="/wizard"
                onClick={onClose}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 px-3 py-3 rounded-lg text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-pastel-blue-dark text-neutral-800'
                      : 'bg-pastel-blue hover:bg-pastel-blue-dark text-neutral-800'
                  )
                }
              >
                <Wand2 className="h-5 w-5" />
                Katalog erstellen
              </NavLink>
            </div>
          </nav>

          {/* Footer */}
          <div className="px-6 py-4 border-t border-neutral-200">
            <div className="flex items-center gap-2 text-xs text-neutral-500">
              <span className="h-2 w-2 rounded-full bg-pastel-green" />
              Backend: Online
            </div>
            <p className="text-xs text-neutral-400 mt-1">v1.0.0</p>
          </div>
        </div>
      </aside>
    </>
  );
}
