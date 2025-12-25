import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { layoutsApi } from '@/api';
import type { LayoutResponse, TextToLayoutRequest, ImageToLayoutRequest } from '@/api';

export function useLayout(id: string) {
  return useQuery({
    queryKey: ['layouts', id],
    queryFn: () => layoutsApi.getById(id),
    enabled: !!id,
  });
}

export function useLayoutVariants(id: string) {
  return useQuery({
    queryKey: ['layouts', id, 'variants'],
    queryFn: () => layoutsApi.getVariants(id),
    enabled: !!id,
  });
}

export function useGenerateFromText() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: TextToLayoutRequest) => layoutsApi.generateFromText(request),
    onSuccess: (data) => {
      queryClient.setQueryData(['layouts', data.id], data);
    },
  });
}

export function useGenerateFromImage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: ImageToLayoutRequest) => layoutsApi.generateFromImage(request),
    onSuccess: (data) => {
      queryClient.setQueryData(['layouts', data.id], data);
    },
  });
}

export function useUpdateLayout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, layout }: { id: string; layout: Partial<LayoutResponse> }) =>
      layoutsApi.update(id, layout),
    onSuccess: (data, { id }) => {
      queryClient.setQueryData(['layouts', id], data);
    },
  });
}

export function useDeleteLayout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: layoutsApi.delete,
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: ['layouts', id] });
    },
  });
}
