import type { ExamplePrompt } from '@/api';
import { Badge, Button } from '@/components/ui';
import { styleToBadgeVariant } from '@/components/ui/Badge';
import type { LayoutStyle } from '@/api';

interface PromptDetailProps {
  prompt: ExamplePrompt;
  onUse?: () => void;
}

export function PromptDetail({ prompt, onUse }: PromptDetailProps) {
  const styleVariant = styleToBadgeVariant[prompt.style as LayoutStyle] || 'default';

  return (
    <div className="space-y-4">
      {prompt.description && (
        <div>
          <h4 className="text-sm font-medium text-neutral-800 mb-1">Beschreibung</h4>
          <p className="text-sm text-neutral-600">{prompt.description}</p>
        </div>
      )}

      <div>
        <h4 className="text-sm font-medium text-neutral-800 mb-2">Prompt</h4>
        <div className="bg-neutral-50 rounded-lg p-4 text-sm text-neutral-700 whitespace-pre-wrap">
          {prompt.prompt}
        </div>
      </div>

      <div>
        <h4 className="text-sm font-medium text-neutral-800 mb-2">Optionen</h4>
        <ul className="text-sm text-neutral-600 space-y-1">
          <li className="flex items-center gap-2">
            <span className="text-neutral-500">Format:</span>
            <Badge variant="default">{prompt.format}</Badge>
          </li>
          <li className="flex items-center gap-2">
            <span className="text-neutral-500">Stil:</span>
            <Badge variant={styleVariant}>{prompt.style}</Badge>
          </li>
        </ul>
      </div>

      {prompt.skills.length > 0 && (
        <div>
          <h4 className="text-sm font-medium text-neutral-800 mb-2">Verwendete Skills</h4>
          <div className="flex flex-wrap gap-2">
            {prompt.skills.map((skill) => (
              <Badge key={skill} variant="blue">
                {skill}
              </Badge>
            ))}
          </div>
        </div>
      )}

      <Button onClick={onUse} className="w-full">
        Im Wizard verwenden
      </Button>
    </div>
  );
}
