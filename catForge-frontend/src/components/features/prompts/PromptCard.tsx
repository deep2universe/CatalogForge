import type { ExamplePrompt } from '@/api';
import { Card, Badge, Button } from '@/components/ui';
import { styleToBadgeVariant } from '@/components/ui/Badge';
import { truncateText } from '@/utils';
import type { LayoutStyle } from '@/api';

interface PromptCardProps {
  prompt: ExamplePrompt;
  onClick?: () => void;
  onUse?: () => void;
}

export function PromptCard({ prompt, onClick, onUse }: PromptCardProps) {
  const styleVariant = styleToBadgeVariant[prompt.style as LayoutStyle] || 'default';

  return (
    <Card hoverable onClick={onClick} className="p-4 flex flex-col">
      <h3 className="font-semibold text-neutral-800 mb-2">{prompt.title}</h3>
      <p className="text-sm text-neutral-600 flex-1 mb-3">
        {truncateText(prompt.description || prompt.prompt, 100)}
      </p>
      <div className="flex flex-wrap gap-2 mb-3">
        <Badge variant={styleVariant}>{prompt.style}</Badge>
        <Badge variant="default">{prompt.format}</Badge>
      </div>
      <Button
        variant="secondary"
        size="sm"
        onClick={(e) => {
          e.stopPropagation();
          onUse?.();
        }}
        className="w-full"
      >
        Verwenden →
      </Button>
    </Card>
  );
}
