import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AppLayout } from '@/components/layout';
import { ToastProvider } from '@/components/ui';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { DashboardPage } from '@/pages/Dashboard/DashboardPage';
import { SkillExplorerPage } from '@/pages/SkillExplorer/SkillExplorerPage';
import { PromptExplorerPage } from '@/pages/PromptExplorer/PromptExplorerPage';
import { CatalogWizardPage } from '@/pages/CatalogWizard/CatalogWizardPage';
import { CatalogPreviewPage } from '@/pages/CatalogPreview/CatalogPreviewPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 Minuten
      retry: 1,
    },
  },
});

function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <BrowserRouter>
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
          </BrowserRouter>
        </ToastProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
