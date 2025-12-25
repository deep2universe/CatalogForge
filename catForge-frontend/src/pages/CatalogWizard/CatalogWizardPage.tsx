import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@/components/layout';
import { Button, Card } from '@/components/ui';
import { 
  StepIndicator, 
  ProductSelector, 
  LayoutOptions, 
  PromptInput, 
  ImageUpload, 
  ChatInterface 
} from '@/components/features/wizard';
import { LayoutPreview, VariantSelector } from '@/components/features/preview';
import { useWizardStore } from '@/store/wizardStore';
import { useGenerateFromText, useGenerateFromImage } from '@/hooks/useLayouts';
import { validatePromptLength } from '@/utils';

const STEPS = [
  { number: 1, label: 'Produkte' },
  { number: 2, label: 'Optionen' },
  { number: 3, label: 'Prompt' },
  { number: 4, label: 'Ergebnis' },
];

export function CatalogWizardPage() {
  const navigate = useNavigate();
  const {
    currentStep,
    setStep,
    nextStep,
    prevStep,
    selectedProducts,
    addProduct,
    removeProduct,
    options,
    setOptions,
    prompt,
    setPrompt,
    referenceImage,
    setReferenceImage,
    chatHistory,
    addChatMessage,
    generatedLayout,
    setGeneratedLayout,
    selectedVariantId,
    setSelectedVariantId,
    isGenerating,
    setIsGenerating,
    setError,
    reset,
  } = useWizardStore();

  const textToLayout = useGenerateFromText();
  const imageToLayout = useGenerateFromImage();

  const canProceed = useCallback(() => {
    switch (currentStep) {
      case 1:
        return selectedProducts.length > 0;
      case 2:
        return true;
      case 3:
        return validatePromptLength(prompt).valid || referenceImage !== null;
      case 4:
        return generatedLayout !== null && selectedVariantId !== null;
      default:
        return false;
    }
  }, [currentStep, selectedProducts, prompt, referenceImage, generatedLayout, selectedVariantId]);

  const handleGenerate = async () => {
    if (!canProceed()) return;

    setIsGenerating(true);
    setError(null);

    // Add user message to chat
    addChatMessage({ role: 'user', content: prompt });

    try {
      const productIds = selectedProducts.map((p) => p.id);
      let result;

      if (referenceImage) {
        result = await imageToLayout.mutateAsync({
          productIds,
          options,
          prompt: prompt || undefined,
          imageBase64: referenceImage.base64,
          imageMimeType: referenceImage.mimeType,
        });
      } else {
        result = await textToLayout.mutateAsync({
          productIds,
          options,
          prompt,
        });
      }

      setGeneratedLayout(result);
      if (result.variants.length > 0) {
        setSelectedVariantId(result.variants[0].id);
      }

      // Add assistant response
      addChatMessage({
        role: 'assistant',
        content: `Ich habe ${result.variantCount} Layout-Variante(n) für Sie generiert. Wählen Sie Ihre bevorzugte Variante aus.`,
      });

      nextStep();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Fehler bei der Generierung';
      setError(message);
      addChatMessage({
        role: 'assistant',
        content: `Es ist ein Fehler aufgetreten: ${message}. Bitte versuchen Sie es erneut.`,
      });
    } finally {
      setIsGenerating(false);
    }
  };

  const handleFinish = () => {
    if (generatedLayout && selectedVariantId) {
      navigate(`/preview/${generatedLayout.id}?variant=${selectedVariantId}`);
    }
  };

  const handleReset = () => {
    reset();
  };

  const selectedVariant = generatedLayout?.variants.find((v) => v.id === selectedVariantId) || null;

  return (
    <PageContainer>
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-neutral-800">Katalog erstellen</h1>
        <p className="text-neutral-600 mt-1">
          Erstellen Sie in 4 Schritten Ihren individuellen Produktkatalog
        </p>
      </div>

      {/* Step Indicator */}
      <div className="mb-8">
        <StepIndicator steps={STEPS} currentStep={currentStep} />
      </div>

      {/* Step Content */}
      <Card className="p-6">
        {currentStep === 1 && (
          <div className="space-y-6">
            <div>
              <h2 className="text-lg font-semibold text-neutral-800 mb-2">
                Produkte auswählen
              </h2>
              <p className="text-neutral-600">
                Wählen Sie die Produkte aus, die in Ihrem Katalog erscheinen sollen.
              </p>
            </div>
            <ProductSelector
              selectedProducts={selectedProducts}
              onAddProduct={addProduct}
              onRemoveProduct={removeProduct}
            />
          </div>
        )}

        {currentStep === 2 && (
          <div className="space-y-6">
            <div>
              <h2 className="text-lg font-semibold text-neutral-800 mb-2">
                Layout-Optionen
              </h2>
              <p className="text-neutral-600">
                Konfigurieren Sie Format, Stil und weitere Optionen für Ihren Katalog.
              </p>
            </div>
            <LayoutOptions options={options} onChange={setOptions} />
          </div>
        )}

        {currentStep === 3 && (
          <div className="space-y-6">
            <div>
              <h2 className="text-lg font-semibold text-neutral-800 mb-2">
                Beschreibung & Referenzbild
              </h2>
              <p className="text-neutral-600">
                Beschreiben Sie, wie Ihr Katalog aussehen soll, oder laden Sie ein Referenzbild hoch.
              </p>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="space-y-4">
                <PromptInput
                  value={prompt}
                  onChange={setPrompt}
                  disabled={isGenerating}
                />
                <ImageUpload
                  value={referenceImage}
                  onChange={setReferenceImage}
                  disabled={isGenerating}
                />
              </div>
              <div>
                <ChatInterface
                  messages={chatHistory}
                  onSendMessage={(content) => {
                    setPrompt(content);
                    handleGenerate();
                  }}
                  isLoading={isGenerating}
                  disabled={isGenerating}
                />
              </div>
            </div>
          </div>
        )}

        {currentStep === 4 && (
          <div className="space-y-6">
            <div>
              <h2 className="text-lg font-semibold text-neutral-800 mb-2">
                Ergebnis
              </h2>
              <p className="text-neutral-600">
                Wählen Sie Ihre bevorzugte Layout-Variante aus.
              </p>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
              <div className="lg:col-span-3">
                <LayoutPreview
                  variant={selectedVariant}
                  pageFormat={generatedLayout?.pageFormat}
                  className="h-[600px]"
                />
              </div>
              <div>
                <VariantSelector
                  variants={generatedLayout?.variants || []}
                  selectedId={selectedVariantId}
                  onSelect={setSelectedVariantId}
                />
              </div>
            </div>
          </div>
        )}
      </Card>

      {/* Navigation */}
      <div className="flex justify-between mt-6">
        <div>
          {currentStep > 1 && (
            <Button variant="secondary" onClick={prevStep} disabled={isGenerating}>
              Zurück
            </Button>
          )}
        </div>
        <div className="flex gap-3">
          <Button variant="ghost" onClick={handleReset} disabled={isGenerating}>
            Neu starten
          </Button>
          {currentStep < 3 && (
            <Button onClick={nextStep} disabled={!canProceed()}>
              Weiter
            </Button>
          )}
          {currentStep === 3 && (
            <Button onClick={handleGenerate} disabled={!canProceed() || isGenerating} isLoading={isGenerating}>
              Generieren
            </Button>
          )}
          {currentStep === 4 && (
            <Button onClick={handleFinish} disabled={!canProceed()}>
              Zur Vorschau
            </Button>
          )}
        </div>
      </div>
    </PageContainer>
  );
}
