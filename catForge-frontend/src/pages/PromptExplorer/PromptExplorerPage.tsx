import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@/components/layout';
import { PromptCard, PromptDetail } from '@/components/features/prompts';
import { Select, Modal, Spinner } from '@/components/ui';
import { useExamplePrompts } from '@/hooks';
import { useWizardStore } from '@/store/wizardStore';
import { filterPrompts } from '@/utils';
import type { ExamplePrompt, LayoutStyle, PageFormat } from '@/api';

const styles: Array<{ value: LayoutStyle | 'all'; label: string }> = [
  { value: 'all', label: 'Alle Stile' },
  { value: 'modern', label: 'Modern' },
  { value: 'technical', label: 'Technical' },
  { value: 'premium', label: 'Premium' },
  { value: 'eco', label: 'Eco' },
  { value: 'dynamic', label: 'Dynamic' },
];

const formats: Array<{ value: PageFormat | 'all'; label: string }> = [
  { value: 'all', label: 'Alle Formate' },
  { value: 'A4', label: 'A4' },
  { value: 'A5', label: 'A5' },
  { value: 'A6', label: 'A6' },
  { value: 'DL', label: 'DL' },
  { value: 'SQUARE', label: 'Quadrat' },
];

export function PromptExplorerPage() {
  const navigate = useNavigate();
  const [selectedStyle, setSelectedStyle] = useState<LayoutStyle | 'all'>('all');
  const [selectedFormat, setSelectedFormat] = useState<PageFormat | 'all'>('all');
  const [selectedPrompt, setSelectedPrompt] = useState<ExamplePrompt | null>(null);

  const { data: prompts, isLoading } = useExamplePrompts();
  const { setPrompt, setOptions } = useWizardStore();

  const filteredPrompts = useMemo(() => {
    if (!prompts) return [];
    return filterPrompts(prompts, selectedStyle, selectedFormat);
  }, [prompts, selectedStyle, selectedFormat]);

  const handleUsePrompt = (prompt: ExamplePrompt) => {
    setPrompt(prompt.prompt);
    setOptions({
      style: prompt.style as LayoutStyle,
      pageFormat: prompt.format as PageFormat,
    });
    navigate('/wizard');
  };

  if (isLoading) {
    return (
      <PageContainer className="flex items-center justify-center min-h-[50vh]">
        <Spinner size="lg" />
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        <Select
          value={selectedStyle}
          onChange={(e) => setSelectedStyle(e.target.value as LayoutStyle | 'all')}
          className="w-40"
        >
          {styles.map((style) => (
            <option key={style.value} value={style.value}>
              {style.label}
            </option>
          ))}
        </Select>

        <Select
          value={selectedFormat}
          onChange={(e) => setSelectedFormat(e.target.value as PageFormat | 'all')}
          className="w-40"
        >
          {formats.map((format) => (
            <option key={format.value} value={format.value}>
              {format.label}
            </option>
          ))}
        </Select>
      </div>

      {/* Prompt Grid */}
      {filteredPrompts.length === 0 ? (
        <div className="text-center py-12 text-neutral-500">
          Keine Prompts gefunden.
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredPrompts.map((prompt) => (
            <PromptCard
              key={prompt.id}
              prompt={prompt}
              onClick={() => setSelectedPrompt(prompt)}
              onUse={() => handleUsePrompt(prompt)}
            />
          ))}
        </div>
      )}

      {/* Prompt Detail Modal */}
      <Modal
        isOpen={!!selectedPrompt}
        onClose={() => setSelectedPrompt(null)}
        title={selectedPrompt?.title}
        className="max-w-xl"
      >
        {selectedPrompt && (
          <PromptDetail
            prompt={selectedPrompt}
            onUse={() => {
              handleUsePrompt(selectedPrompt);
              setSelectedPrompt(null);
            }}
          />
        )}
      </Modal>
    </PageContainer>
  );
}
