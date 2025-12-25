import ReactMarkdown from 'react-markdown';
import type { Skill } from '@/api';
import { Badge } from '@/components/ui';

interface SkillDetailProps {
  skill: Skill;
  onDependencyClick?: (name: string) => void;
}

const categoryColors: Record<string, 'blue' | 'purple' | 'orange'> = {
  core: 'blue',
  styles: 'purple',
  formats: 'orange',
};

export function SkillDetail({ skill, onDependencyClick }: SkillDetailProps) {
  return (
    <div className="space-y-4">
      <div className="flex items-center gap-4">
        <Badge variant={categoryColors[skill.category] || 'default'}>
          {skill.category}
        </Badge>
        <span className="text-sm text-neutral-500">Priorität: {skill.priority}</span>
      </div>

      {skill.dependencies.length > 0 && (
        <div>
          <h4 className="text-sm font-medium text-neutral-800 mb-2">Abhängigkeiten</h4>
          <div className="flex flex-wrap gap-2">
            {skill.dependencies.map((dep) => (
              <button
                key={dep}
                onClick={() => onDependencyClick?.(dep)}
                className="px-2 py-1 text-xs bg-neutral-100 hover:bg-neutral-200 rounded transition-colors"
              >
                {dep}
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="border-t border-neutral-200 pt-4">
        <h4 className="text-sm font-medium text-neutral-800 mb-2">Inhalt</h4>
        <div className="prose prose-sm max-w-none text-neutral-600">
          <ReactMarkdown>{skill.content}</ReactMarkdown>
        </div>
      </div>
    </div>
  );
}
