import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

export const uploadPdf = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await api.post('/pdf/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
};

export const rewriteText = async (originalText, instruction) => {
  const response = await api.post('/edit/rewrite', {
    originalText,
    instruction
  });
  return response.data;
};

export const exportEditedPdf = async (documentId, edits) => {
  const response = await api.post('/pdf/export', {
    documentId,
    edits
  }, {
    responseType: 'blob'
  });
  return response.data;
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
  const response = await api.post('/fluid/export', {
    htmlContent
  }, {
    responseType: 'blob'
  });
  return response.data;
};
