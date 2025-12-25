import { useState, useMemo } from 'react';
import { PageContainer } from '@/components/layout';
import { SkillCard, SkillDetail, CategoryFilter } from '@/components/features/skills';
import { Card, Modal, Spinner } from '@/components/ui';
import { useSkills, useSkillCategories } from '@/hooks';
import { filterSkills, sortSkills } from '@/utils';
import type { Skill } from '@/api';

export function SkillExplorerPage() {
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedSkill, setSelectedSkill] = useState<Skill | null>(null);

  const { data: skills, isLoading: skillsLoading } = useSkills();
  const { data: categories } = useSkillCategories();

  const filteredSkills = useMemo(() => {
    if (!skills) return [];
    const filtered = filterSkills(skills, selectedCategory === 'all' ? undefined : selectedCategory);
    return sortSkills(filtered);
  }, [skills, selectedCategory]);

  const handleDependencyClick = (name: string) => {
    const skill = skills?.find((s) => s.name === name);
    if (skill) {
      setSelectedSkill(skill);
    }
  };

  if (skillsLoading) {
    return (
      <PageContainer className="flex items-center justify-center min-h-[50vh]">
        <Spinner size="lg" />
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <div className="flex flex-col md:flex-row gap-6">
        {/* Sidebar Filter */}
        <aside className="w-full md:w-48 flex-shrink-0">
          <Card className="p-4">
            <h3 className="font-medium text-neutral-800 mb-3">Filter</h3>
            <CategoryFilter
              categories={categories ?? []}
              selected={selectedCategory}
              onChange={setSelectedCategory}
            />
          </Card>
        </aside>

        {/* Skill Grid */}
        <main className="flex-1">
          {filteredSkills.length === 0 ? (
            <div className="text-center py-12 text-neutral-500">
              Keine Skills gefunden.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredSkills.map((skill) => (
                <SkillCard
                  key={skill.name}
                  skill={skill}
                  onClick={() => setSelectedSkill(skill)}
                />
              ))}
            </div>
          )}
        </main>
      </div>

      {/* Skill Detail Modal */}
      <Modal
        isOpen={!!selectedSkill}
        onClose={() => setSelectedSkill(null)}
        title={selectedSkill?.name.replace(/_/g, ' ')}
        className="max-w-2xl"
      >
        {selectedSkill && (
          <SkillDetail
            skill={selectedSkill}
            onDependencyClick={handleDependencyClick}
          />
        )}
      </Modal>
    </PageContainer>
  );
}
