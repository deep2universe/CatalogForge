import { useQuery, useMutation } from '@tanstack/react-query';
import { pdfApi } from '@/api';
import type { PdfGenerateRequest } from '@/api';

export function usePrintPresets() {
  return useQuery({
    queryKey: ['pdf', 'presets'],
    queryFn: pdfApi.getPresets,
  });
}

export function useGeneratePdf() {
  return useMutation({
    mutationFn: (request: PdfGenerateRequest) => pdfApi.generate(request),
  });
}

export function useDownloadPdf() {
  return useMutation({
    mutationFn: async (id: string) => {
      const blob = await pdfApi.download(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `katalog-${id}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    },
  });
}
