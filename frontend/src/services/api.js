import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export const uploadPdf = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await api.post('/pdf/upload', formData);
  return response.data;
};

// ─── V2+ Preparations ──────────────────────────────────────────

export const extractFonts = async (documentId) => {
  try {
    const response = await api.get(`/tools/extract-fonts`, {
      params: { documentId }
    });
    return response.data || [];
  } catch (error) {
    console.warn('V2 API Error: Failed to extract fonts. Safe to ignore in V1.', error);
    return []; // Graceful degradation for V1
  }
};

export const rewriteText = async (originalText, instruction) => {
  const response = await api.post('/edit/rewrite', {
    originalText,
    instruction
  });
  return response.data;
};

export const exportEditedPdf = async (documentId, edits) => {
  const response = await fetch('http://localhost:8080/api/pdf/export', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ documentId, edits })
  });
  if (!response.ok) throw new Error('Export failed');
  return await response.blob();
};

export const getFluidText = async (documentId) => {
  try {
    const response = await api.get(`/fluid/${documentId}/text`);
    if (response.data.error) {
      throw new Error(response.data.error);
    }
    return response.data.text || '';
  } catch (error) {
    console.error('API Error getting fluid text:', error.response?.data || error.message);
    throw error;
  }
};

export const exportFluidPdf = async (htmlContent) => {
  const response = await fetch('http://localhost:8080/api/fluid/export', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ htmlContent })
  });
  if (!response.ok) throw new Error('Export failed');
  return await response.blob();
};
