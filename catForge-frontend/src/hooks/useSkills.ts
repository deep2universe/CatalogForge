import { useQuery } from '@tanstack/react-query';
import { skillsApi } from '@/api';

export function useSkills() {
  return useQuery({
    queryKey: ['skills'],
    queryFn: skillsApi.getAll,
  });
}

export function useSkillCategories() {
  return useQuery({
    queryKey: ['skills', 'categories'],
    queryFn: skillsApi.getCategories,
  });
}

export function useSkillsByCategory(category: string) {
  return useQuery({
    queryKey: ['skills', category],
    queryFn: () => skillsApi.getByCategory(category),
    enabled: !!category,
  });
}

export function useExamplePrompts() {
  return useQuery({
    queryKey: ['skills', 'prompts', 'examples'],
    queryFn: skillsApi.getExamplePrompts,
  });
}
