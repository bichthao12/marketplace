import type { ReactNode } from 'react';

interface LayoutProps {
  title: string;
  nav?: ReactNode;
  actions?: ReactNode;
  children: ReactNode;
}

export function Layout({ title, nav, actions, children }: LayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4">
          <div className="flex items-center gap-6">
            <h1 className="text-lg font-semibold text-gray-900">{title}</h1>
            {nav && <nav className="flex flex-wrap items-center gap-3 text-sm">{nav}</nav>}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">{children}</main>
    </div>
  );
}

interface CardProps {
  children: ReactNode;
  className?: string;
}

export function Card({ children, className = '' }: CardProps) {
  return (
    <div className={`rounded-lg border border-gray-200 bg-white p-4 shadow-sm ${className}`}>
      {children}
    </div>
  );
}
