import { useState } from 'react';
import { cn } from '@/utils/cn';
import { Button } from '@/components/ui';

interface CodeEditorProps {
  html: string;
  css: string;
  onHtmlChange?: (html: string) => void;
  onCssChange?: (css: string) => void;
  readOnly?: boolean;
}

type Tab = 'html' | 'css';

export function CodeEditor({
  html,
  css,
  onHtmlChange,
  onCssChange,
  readOnly = false,
}: CodeEditorProps) {
  const [activeTab, setActiveTab] = useState<Tab>('html');
  const [copied, setCopied] = useState(false);

  const currentCode = activeTab === 'html' ? html : css;
  const handleChange = activeTab === 'html' ? onHtmlChange : onCssChange;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(currentCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  return (
    <div className="flex flex-col h-full border border-neutral-200 rounded-lg overflow-hidden">
      {/* Tabs */}
      <div className="flex items-center justify-between border-b border-neutral-200 bg-neutral-50 px-2">
        <div className="flex">
          <button
            onClick={() => setActiveTab('html')}
            className={cn(
              'px-4 py-2 text-sm font-medium transition-colors',
              activeTab === 'html'
                ? 'text-neutral-800 border-b-2 border-pastel-blue'
                : 'text-neutral-500 hover:text-neutral-700'
            )}
          >
            HTML
          </button>
          <button
            onClick={() => setActiveTab('css')}
            className={cn(
              'px-4 py-2 text-sm font-medium transition-colors',
              activeTab === 'css'
                ? 'text-neutral-800 border-b-2 border-pastel-blue'
                : 'text-neutral-500 hover:text-neutral-700'
            )}
          >
            CSS
          </button>
        </div>
        <Button variant="ghost" size="sm" onClick={handleCopy}>
          {copied ? 'Kopiert!' : 'Kopieren'}
        </Button>
      </div>

      {/* Editor */}
      <div className="flex-1 overflow-auto">
        <textarea
          value={currentCode}
          onChange={(e) => handleChange?.(e.target.value)}
          readOnly={readOnly}
          spellCheck={false}
          className={cn(
            'w-full h-full p-4 font-mono text-sm bg-neutral-900 text-neutral-100 resize-none',
            'focus:outline-none',
            readOnly && 'cursor-default'
          )}
        />
      </div>
    </div>
  );
}
