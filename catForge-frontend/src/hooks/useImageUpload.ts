import { useMutation } from '@tanstack/react-query';
import { imagesApi } from '@/api';

export function useImageUpload() {
  return useMutation({
    mutationFn: (file: File) => imagesApi.upload(file),
  });
}

export function useImageUploadBase64() {
  return useMutation({
    mutationFn: ({ base64Data, mimeType, filename }: { 
      base64Data: string; 
      mimeType: string; 
      filename?: string;
    }) => imagesApi.uploadBase64(base64Data, mimeType, filename),
  });
}

export function fileToBase64(file: File): Promise<{ base64: string; mimeType: string }> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      const base64 = result.split(',')[1];
      resolve({ base64, mimeType: file.type });
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}
