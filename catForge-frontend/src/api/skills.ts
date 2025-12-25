import { apiClient } from './client';
import type { Skill, ExamplePrompt } from './types';

export const skillsApi = {
  getAll: () => apiClient<Skill[]>('/skills'),

  getCategories: () => apiClient<string[]>('/skills/categories'),

  getByCategory: (category: string) =>
    apiClient<Skill[]>(`/skills/${category}`),

  getExamplePrompts: () =>
    apiClient<ExamplePrompt[]>('/skills/prompts/examples'),

  reload: () => apiClient<void>('/skills/reload', { method: 'POST' }),
};
