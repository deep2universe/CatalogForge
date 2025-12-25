import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppLayout } from '@/components/layout';
import { ToastProvider, Spinner } from '@/components/ui';
import { ErrorBoundary } from '@/components/ErrorBoundary';

// Lazy load pages for code splitting
const DashboardPage = lazy(() => import('@/pages/Dashboard/DashboardPage').then(m => ({ default: m.DashboardPage })));
const SkillExplorerPage = lazy(() => import('@/pages/SkillExplorer/SkillExplorerPage').then(m => ({ default: m.SkillExplorerPage })));
const PromptExplorerPage = lazy(() => import('@/pages/PromptExplorer/PromptExplorerPage').then(m => ({ default: m.PromptExplorerPage })));
const CatalogWizardPage = lazy(() => import('@/pages/CatalogWizard/CatalogWizardPage').then(m => ({ default: m.CatalogWizardPage })));
const CatalogPreviewPage = lazy(() => import('@/pages/CatalogPreview/CatalogPreviewPage').then(m => ({ default: m.CatalogPreviewPage })));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 Minuten
      retry: 1,
    },
  },
});

function PageLoader() {
  return (
    <div className="flex items-center justify-center min-h-[50vh]">
      <Spinner size="lg" />
    </div>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <BrowserRouter>
            <Suspense fallback={<PageLoader />}>
              <Routes>
                <Route element={<AppLayout />}>
                  <Route path="/" element={<DashboardPage />} />
                  <Route path="/skills" element={<SkillExplorerPage />} />
                  <Route path="/prompts" element={<PromptExplorerPage />} />
                  <Route path="/wizard" element={<CatalogWizardPage />} />
                  <Route path="/preview/:id" element={<CatalogPreviewPage />} />
                  <Route path="*" element={<Navigate to="/" replace />} />
                </Route>
              </Routes>
            </Suspense>
          </BrowserRouter>
        </ToastProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
