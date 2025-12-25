import type { Skill } from '@/api';
import { Card, Badge } from '@/components/ui';

interface SkillCardProps {
  skill: Skill;
  onClick?: () => void;
}

const categoryColors: Record<string, 'blue' | 'purple' | 'orange'> = {
  core: 'blue',
  styles: 'purple',
  formats: 'orange',
};

export function SkillCard({ skill, onClick }: SkillCardProps) {
  return (
    <Card hoverable onClick={onClick} className="p-4">
      <div className="flex items-start justify-between gap-2 mb-2">
        <h3 className="font-semibold text-neutral-800 text-sm uppercase tracking-wide">
          {skill.name.replace(/_/g, ' ')}
        </h3>
        <Badge variant={categoryColors[skill.category] || 'default'}>
          {skill.category}
        </Badge>
      </div>
      <div className="flex items-center gap-2 text-xs text-neutral-500">
        <span>Priorität: {skill.priority}</span>
        {skill.dependencies.length > 0 && (
          <span>• {skill.dependencies.length} Abhängigkeiten</span>
        )}
      </div>
    </Card>
  );
}
