import { useState } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { PageContainer } from '@/components/layout';
import { Button, Card, Modal, Spinner } from '@/components/ui';
import { 
  LayoutPreview, 
  VariantSelector, 
  CodeEditor, 
  PdfExport 
} from '@/components/features/preview';
import { useLayout, useDeleteLayout } from '@/hooks/useLayouts';

type ViewMode = 'preview' | 'code';

export function CatalogPreviewPage() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const initialVariantId = searchParams.get('variant');
  const [selectedVariantId, setSelectedVariantId] = useState<string | null>(initialVariantId);
  const [viewMode, setViewMode] = useState<ViewMode>('preview');
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const { data: layout, isLoading, error } = useLayout(id || '');
  const deleteLayout = useDeleteLayout();

  const selectedVariant = layout?.variants.find((v) => v.id === selectedVariantId) || 
    layout?.variants[0] || null;

  const handleDelete = async () => {
    if (!id) return;
    try {
      await deleteLayout.mutateAsync(id);
      navigate('/');
    } catch (err) {
      console.error('Delete failed:', err);
    }
  };

  const handleBackToWizard = () => {
    navigate('/wizard');
  };

  if (isLoading) {
    return (
      <PageContainer>
        <div className="flex items-center justify-center h-96">
          <Spinner size="lg" />
        </div>
      </PageContainer>
    );
  }

  if (error || !layout) {
    return (
      <PageContainer>
        <div className="text-center py-12">
          <h2 className="text-xl font-semibold text-neutral-800 mb-2">
            Layout nicht gefunden
          </h2>
          <p className="text-neutral-600 mb-4">
            Das angeforderte Layout existiert nicht oder wurde gelöscht.
          </p>
          <Button onClick={handleBackToWizard}>
            Neuen Katalog erstellen
          </Button>
        </div>
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-neutral-800">Katalog-Vorschau</h1>
          <p className="text-neutral-600 mt-1">
            Layout ID: {layout.id} · Format: {layout.pageFormat} · {layout.variantCount} Variante(n)
          </p>
        </div>
        <div className="flex gap-3">
          <Button variant="ghost" onClick={handleBackToWizard}>
            Neuer Katalog
          </Button>
          <Button variant="danger" onClick={() => setShowDeleteModal(true)}>
            Löschen
          </Button>
        </div>
      </div>

      {/* View Mode Toggle */}
      <div className="flex gap-2 mb-6">
        <Button
          variant={viewMode === 'preview' ? 'primary' : 'secondary'}
          onClick={() => setViewMode('preview')}
        >
          Vorschau
        </Button>
        <Button
          variant={viewMode === 'code' ? 'primary' : 'secondary'}
          onClick={() => setViewMode('code')}
        >
          Code
        </Button>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Preview/Code Area */}
        <div className="lg:col-span-3">
          <Card className="p-4 h-[600px]">
            {viewMode === 'preview' ? (
              <LayoutPreview
                variant={selectedVariant}
                pageFormat={layout.pageFormat}
                className="h-full"
              />
            ) : (
              <CodeEditor
                html={selectedVariant?.html || ''}
                css={selectedVariant?.css || ''}
                readOnly
              />
            )}
          </Card>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Variant Selector */}
          <Card className="p-4">
            <VariantSelector
              variants={layout.variants}
              selectedId={selectedVariantId || selectedVariant?.id || null}
              onSelect={setSelectedVariantId}
            />
          </Card>

          {/* PDF Export */}
          <Card className="p-4">
            <PdfExport
              layoutId={layout.id}
              variantId={selectedVariantId || selectedVariant?.id}
            />
          </Card>

          {/* Layout Info */}
          <Card className="p-4">
            <h3 className="text-sm font-medium text-neutral-800 mb-3">Details</h3>
            <dl className="space-y-2 text-sm">
              <div className="flex justify-between">
                <dt className="text-neutral-500">Erstellt</dt>
                <dd className="text-neutral-800">
                  {new Date(layout.generatedAt).toLocaleString('de-DE')}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-neutral-500">Status</dt>
                <dd className="text-neutral-800">{layout.status}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-neutral-500">Format</dt>
                <dd className="text-neutral-800">{layout.pageFormat}</dd>
              </div>
            </dl>
          </Card>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        title="Layout löschen"
      >
        <div className="space-y-4">
          <p className="text-neutral-600">
            Möchten Sie dieses Layout wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.
          </p>
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
              Abbrechen
            </Button>
            <Button
              variant="danger"
              onClick={handleDelete}
              isLoading={deleteLayout.isPending}
            >
              Löschen
            </Button>
          </div>
        </div>
      </Modal>
    </PageContainer>
  );
}
