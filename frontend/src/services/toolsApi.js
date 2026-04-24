const API_URL = 'http://localhost:8080/api';

export const compressPdf = async (documentId, quality = 'medium') => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('quality', quality);

  const response = await fetch(`${API_URL}/tools/compress`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('Compress failed');
  return await response.blob();
};

export const addPassword = async (documentId, userPassword, ownerPassword = '', allowPrint = true, allowCopy = false) => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('userPassword', userPassword);
  if (ownerPassword) params.append('ownerPassword', ownerPassword);
  params.append('allowPrint', allowPrint);
  params.append('allowCopy', allowCopy);

  const response = await fetch(`${API_URL}/tools/password`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('Password protection failed');
  return await response.blob();
};

export const mergePdfs = async (files) => {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append('files', file);
  });

  const response = await fetch(`${API_URL}/tools/merge`, {
    method: 'POST',
    body: formData,
  });
  if (!response.ok) throw new Error('Merge failed');
  return await response.blob();
};

export const addWatermark = async (documentId, config = {}) => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('text', config.text || 'WATERMARK');
  params.append('fontSize', config.fontSize || 48);
  params.append('opacity', config.opacity || 0.3);
  params.append('rotation', config.rotation || 45);
  params.append('color', config.color || '#CCCCCC');

  const response = await fetch(`${API_URL}/tools/watermark`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('Watermark failed');
  return await response.blob();
};

export const addStamp = async (documentId, stampFile, config = {}) => {
  const formData = new FormData();
  formData.append('documentId', documentId);
  formData.append('stampImage', stampFile);
  formData.append('position', config.position || 'center');
  formData.append('scale', config.scale || 0.5);
  formData.append('opacity', config.opacity || 0.8);
  formData.append('pageSelection', config.pageSelection || 'all');

  const response = await fetch(`${API_URL}/tools/stamp`, {
    method: 'POST',
    body: formData,
  });
  if (!response.ok) throw new Error('Stamp failed');
  return await response.blob();
};

// ─── Phase 2 Tools ───────────────────────────────────────────

export const splitPdf = async (documentId, pages) => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('pages', pages);

  const response = await fetch(`${API_URL}/tools/split`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('Split failed');
  return await response.blob();
};

export const arrangePdf = async (documentId, pages) => {
  const response = await fetch(`${API_URL}/tools/arrange`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ documentId, pages }),
  });
  if (!response.ok) throw new Error('Arrange failed');
  return await response.blob();
};

export const convertPdf = async (documentId, format = 'png', dpi = 150) => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('format', format);
  params.append('dpi', dpi);

  const response = await fetch(`${API_URL}/tools/convert`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('Convert failed');
  return await response.blob();
};

export const comparePdfs = async (file1, file2) => {
  const formData = new FormData();
  formData.append('file1', file1);
  formData.append('file2', file2);

  const response = await fetch(`${API_URL}/tools/compare`, {
    method: 'POST',
    body: formData,
  });
  if (!response.ok) throw new Error('Compare failed');
  return await response.json();
};

// ─── Phase 3 Tools ───────────────────────────────────────────

export const redactPdf = async (documentId, searchText, color = '#000000') => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('searchText', searchText);
  params.append('color', color);

  const response = await fetch(`${API_URL}/tools/hide-text`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('Redact failed');
  return await response.blob();
};

export const redactPdfBoxes = async (redactRequestDto) => {
  const response = await fetch(`${API_URL}/tools/redact-box`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(redactRequestDto),
  });
  if (!response.ok) throw new Error('Redact boxes failed');
  return await response.blob();
};

export const ocrPdf = async (documentId, language = 'eng') => {
  const params = new URLSearchParams();
  params.append('documentId', documentId);
  params.append('language', language);

  const response = await fetch(`${API_URL}/tools/ocr`, {
    method: 'POST',
    body: params,
  });
  if (!response.ok) throw new Error('OCR failed');
  return await response.blob();
};

export const signPdf = async (documentId, signatureFile, page, x, y, scale) => {
  const formData = new FormData();
  formData.append('documentId', documentId);
  formData.append('signatureFile', signatureFile);
  formData.append('page', page);
  formData.append('x', x);
  formData.append('y', y);
  formData.append('scale', scale);

  const response = await fetch(`${API_URL}/tools/sign`, {
    method: 'POST',
    body: formData,
  });
  if (!response.ok) throw new Error('Sign failed');
  return await response.blob();
};

export const drawOnPdf = async (drawRequestDto) => {
  const response = await fetch(`${API_URL}/tools/draw`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(drawRequestDto),
  });
  if (!response.ok) throw new Error('Draw failed');
  return await response.blob();
};

export const insertImageOnPdf = async (documentId, imageFile, page, xPct, yPct, scale) => {
  const formData = new FormData();
  formData.append('documentId', documentId);
  formData.append('image', imageFile);
  formData.append('page', page);
  formData.append('xPct', xPct);
  formData.append('yPct', yPct);
  formData.append('scale', scale);

  const response = await fetch(`${API_URL}/tools/insertImage`, {
    method: 'POST',
    body: formData,
  });
  if (!response.ok) throw new Error('Insert Image failed');
  return await response.blob();
};
