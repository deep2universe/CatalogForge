import { Menu } from 'lucide-react';

interface HeaderProps {
  title: string;
  onMenuClick: () => void;
}

export function Header({ title, onMenuClick }: HeaderProps) {
  return (
    <header className="sticky top-0 z-30 bg-white border-b border-neutral-200">
      <div className="flex items-center gap-4 px-6 py-4">
        <button
          onClick={onMenuClick}
          className="p-2 rounded-lg hover:bg-neutral-100 md:hidden"
          aria-label="Menü öffnen"
        >
          <Menu className="h-5 w-5 text-neutral-600" />
        </button>
        <h1 className="text-xl font-semibold text-neutral-800">{title}</h1>
      </div>
    </header>
  );
}
