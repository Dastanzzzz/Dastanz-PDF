import React, { useState } from 'react';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import Sidebar from './components/Sidebar';
import RightPanel from './components/RightPanel';
import PdfViewer from './components/PdfViewer';
import FluidEditor from './components/FluidEditor';
import ToolPanel from './components/ToolPanel';
import { uploadPdf, getFluidText } from './services/api';

function App() {
  const [fileUrl, setFileUrl] = useState(null);
  const [documentId, setDocumentId] = useState(null);
  const [textBlocks, setTextBlocks] = useState([]);
  const [appliedEdits, setAppliedEdits] = useState([]);
  
  const [selectedBlock, setSelectedBlock] = useState(null);
  const [loading, setLoading] = useState(false);
  const [editMode, setEditMode] = useState('fixed'); // 'fixed' or 'fluid'
  const [fluidContent, setFluidContent] = useState('');
  const [selectedTool, setSelectedTool] = useState(null);
  const [toolState, setToolState] = useState(null); // Used to pass visual overlay state to PdfViewer

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setLoading(true);
    try {
      const res = await uploadPdf(file);
      setDocumentId(res.documentId);
      setTextBlocks(res.textBlocks || []);
      setFileUrl(URL.createObjectURL(file));
      setAppliedEdits([]);
      setFluidContent('');
      setSelectedBlock(null);

      // If already in fluid mode, fetch the new text immediately
      if (editMode === 'fluid') {
        const text = await getFluidText(res.documentId);
        setFluidContent(text);
      }
    } catch (e) {
      console.error(e);
      alert('Upload failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleModeChange = async (mode) => {
    if (mode === 'fluid' && !fluidContent && documentId) {
      setLoading(true);
      try {
        const text = await getFluidText(documentId);
        setFluidContent(text);
        setEditMode(mode);
      } catch (e) {
        console.error('Failed to load fluid text:', e);
        alert('Failed to load document text. Please try uploading the PDF again.');
        setLoading(false);
        return; // Don't switch mode on error
      }
      setLoading(false);
    } else {
      setEditMode(mode);
    }
  };

  const handleApplyEdit = (editResult) => {
    setAppliedEdits([...appliedEdits.filter(e => e.blockId !== editResult.id), {
      blockId: editResult.id,
      pageNumber: editResult.pageNumber,
      originalText: editResult.text,
      newText: editResult.newText,
      x: editResult.x,
      y: editResult.y,
      width: editResult.width,
      height: editResult.height,
      fontSize: editResult.fontSize
    }]);
    setSelectedBlock(null);
  };

  const handleFluidExport = async (htmlContent) => {
    if (!htmlContent) {
      alert('No content to export');
      return;
    }

    try {
      // Create a temporary container off-screen
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = htmlContent;

      // Ensure off-screen processing without preview zoom restrictions
      tempDiv.style.position = 'absolute';
      tempDiv.style.left = '-9999px';
      tempDiv.style.top = '0';
      tempDiv.style.backgroundColor = 'white';
      
      document.body.appendChild(tempDiv);

      const pages = tempDiv.querySelectorAll('.pdf-page-content');
      if (pages.length === 0) {
        throw new Error('No pages found to export.');
      }

      let pdf = null;

      for (let i = 0; i < pages.length; i++) {
        const pageEl = pages[i];
        
        // Remove simulated visual boundaries from editor view
        pageEl.style.margin = '0';
        pageEl.style.padding = '0';
        pageEl.style.boxShadow = 'none';
        pageEl.style.border = 'none';

        // 1. Computes the PDF page size from the editor document size
        const ptWidth = parseFloat(pageEl.style.width) || 595.28;
        const ptHeight = parseFloat(pageEl.style.height) || 841.89;

        if (!pdf) {
          pdf = new jsPDF({
            orientation: ptWidth > ptHeight ? 'landscape' : 'portrait',
            unit: 'pt', // Use points to match CSS pixel values 1:1
            format: [ptWidth, ptHeight]
          });
        } else {
          pdf.addPage([ptWidth, ptHeight], ptWidth > ptHeight ? 'landscape' : 'portrait');
        }

        // 4. Does not reuse preview zoom (tempDiv is not under the scaled container)
        // Render crisp canvas avoiding double layout scale
        const canvas = await html2canvas(pageEl, {
          scale: 2, // Physical pixel resolution multiplier (independent of layout)
          useCORS: true,
          logging: false,
          backgroundColor: '#ffffff'
        });

        const imgData = canvas.toDataURL('image/jpeg', 0.95);
        
        // 2 & 3. Uses full rendered bounds & inserts at full size (no 10mm margins)
        pdf.addImage(imgData, 'JPEG', 0, 0, ptWidth, ptHeight);
      }

      // Clean up
      document.body.removeChild(tempDiv);

      // Download PDF
      pdf.save('edited-document.pdf');
      alert('PDF exported successfully!');
    } catch (error) {
      console.error('Export error:', error);
      alert('Failed to export PDF: ' + error.message);
    }
  };

  return (
    <div className="flex h-screen w-full bg-slate-100 overflow-hidden font-sans">
      <Sidebar 
        documentId={documentId} 
        editMode={editMode}
        onModeChange={handleModeChange}
        selectedTool={selectedTool}
        onToolSelect={setSelectedTool}
      />
      
      <main className="flex-1 flex flex-col relative h-full">
        {!fileUrl && (
          <div className="absolute inset-0 flex items-center justify-center bg-slate-100/80 backdrop-blur z-10">
            <div className="bg-white p-8 rounded-lg shadow-xl text-center max-w-md w-full">
              <h2 className="text-2xl font-bold mb-4 text-slate-800">Upload PDF</h2>
              <p className="text-slate-500 mb-6 text-sm">Select a digital PDF to extract text blocks and begin editing.</p>
              
              <label className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded cursor-pointer transition-colors block font-medium">
                {loading ? 'Processing...' : 'Choose PDF File'}
                <input type="file" className="hidden" accept="application/pdf" onChange={handleFileUpload} disabled={loading} />
              </label>
            </div>
          </div>
        )}
        
        {editMode === 'fixed' ? (
          <PdfViewer 
            file={fileUrl} 
            textBlocks={textBlocks} 
            appliedEdits={appliedEdits}
            onSelectBlock={setSelectedBlock}
            toolState={toolState}
          />
        ) : (
          <FluidEditor 
            content={fluidContent}
            onChange={setFluidContent}
            onExport={handleFluidExport}
            file={fileUrl}
          />
        )}
      </main>

      {selectedTool ? (
        <ToolPanel
          tool={selectedTool}
          documentId={documentId}
          onClose={() => setSelectedTool(null)}
          toolState={toolState}
          setToolState={setToolState}
        />
      ) : editMode === 'fixed' ? (
        <RightPanel 
          selectedBlock={selectedBlock} 
          handleApplyEdit={handleApplyEdit}
          handleCancelEdit={() => setSelectedBlock(null)}
        />
      ) : null}
    </div>
  );
}

export default App;
