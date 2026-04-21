import React, { useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

// Use local worker for Vite compatibility with react-pdf v9+
pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString();

export default function PdfViewer({ file, textBlocks, appliedEdits, onSelectBlock, toolState }) {
  const [numPages, setNumPages] = useState(null);
  const [pageNumber, setPageNumber] = useState(1);
  const [scale, setScale] = useState(1.2);

  const onDocumentLoadSuccess = ({ numPages }) => {
    setNumPages(numPages);
  };

  const getBlocksForPage = (page) => {
    return textBlocks.filter(b => b.pageNumber === page);
  };

  const getEditForBlock = (blockId) => {
    return appliedEdits.find(e => e.blockId === blockId);
  };

  if (!file) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-slate-400 bg-slate-100">
        <p>No document selected</p>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-100">
      <div className="bg-white border-b py-2 flex justify-center gap-4 items-center mb-4">
         <button disabled={pageNumber <= 1} onClick={() => setPageNumber(p => p - 1)} className="px-3 border rounded hover:bg-slate-50">Prev</button>
         <span className="text-sm">Page {pageNumber} of {numPages}</span>
         <button disabled={pageNumber >= numPages} onClick={() => setPageNumber(p => p + 1)} className="px-3 border rounded hover:bg-slate-50">Next</button>
         
         <div className="w-px h-6 bg-slate-300 mx-2" />
         
         <button onClick={() => setScale(s => s - 0.2)} className="px-2 border rounded hover:bg-slate-50">-</button>
         <span className="text-sm">{(scale * 100).toFixed(0)}%</span>
         <button onClick={() => setScale(s => s + 0.2)} className="px-2 border rounded hover:bg-slate-50">+</button>
      </div>

      <div className="flex-1 overflow-auto flex flex-col items-center pb-12 relative bg-slate-100 p-4">
        <Document file={file} onLoadSuccess={onDocumentLoadSuccess}>
           <div className="relative shadow-xl border border-slate-300 bg-white inline-block">
             <Page 
               pageNumber={pageNumber} 
               scale={scale}
               renderAnnotationLayer={false}
               renderTextLayer={false}
             />
             
             {/* V2 Preview: SVG Overlay for Rich Interaction (Polygons / Custom Shapes) */}
             <svg className="absolute top-0 left-0 w-full h-full pointer-events-none" style={{ zIndex: 5 }}>
               {getBlocksForPage(pageNumber).map(block => {
                 // If V2 backend provides rich polygon data, we will draw it here. 
                 // Otherwise, we fallback to drawing nothing in SVG and let the V1 <div> handle it.
                 if (block.polygonPoints && block.polygonPoints.length > 0) {
                    const scaledPoints = block.polygonPoints
                      .map((pt, i) => i % 2 === 0 ? pt * scale : pt * scale)
                      .join(' ');
                    
                    return (
                        <polygon 
                          key={`poly-${block.id}`} 
                          points={scaledPoints} 
                          fill="rgba(59, 130, 246, 0.1)" 
                          stroke="rgba(37, 99, 235, 0.4)" 
                          strokeWidth="2" 
                          pointerEvents="visiblePainted"
                          cursor="pointer"
                          onClick={() => onSelectBlock(block)}
                        />
                    );
                 }
                 return null;
               })}
             </svg>

             {/* V1 Overlay: Div-based absolute positioning (Fallback) */}
             <div className="absolute top-0 left-0 w-full h-full pointer-events-none overflow-hidden" style={{ zIndex: 10 }}>
               {getBlocksForPage(pageNumber).map(block => {
                 const edit = getEditForBlock(block.id);
                 
                 // Apply scaling directly to coordinates.
                 // We shift Y up by block.height * 0.8 because PDFBox extraction typically returns baseline-heavy coordinates
                 return (
                   <div 
                     key={block.id}
                     onClick={() => onSelectBlock(block)}
                     className={`absolute pointer-events-auto cursor-pointer transition-all flex items-center justify-start overflow-hidden ${
                       edit ? 'bg-white z-10' : 'border-2 border-blue-400/60 hover:bg-blue-400/20 hover:border-blue-600'
                     }`}
                     style={{
                       left: `${block.x * scale}px`,
                       top: `${(block.y - block.height * 0.2) * scale}px`, // Slight 20% shift upwards to perfectly cover the original text bounds
                       width: `${(block.width + 10) * scale}px`, // Slight horizontal padding to redact fully
                       height: `${(block.height * 1.6) * scale}px`, // Total coverage height
                       opacity: (block.polygonPoints && block.polygonPoints.length > 0 && !edit) ? 0 : 1, // Hide V1 box border if V2 SVG polygon is active, unless edited
                     }}
                   >
                     {edit && (
                       <span style={{ 
                         fontSize: `${block.fontSize * scale}px`, // Use actual PDF point size
                         // V2+ Preparation: use extracted font if available, else fallback
                         fontFamily: block.originalFontId || 'Arial, Helvetica, sans-serif',
                         color: 'black', 
                         lineHeight: '1.2',
                         letterSpacing: '-0.3px', // Emulate typical PDF tight kerning
                       }}>
                         {edit.newText}
                       </span>
                     )}
                   </div>
                 )
               })}
               
             </div>
             
             {/* Signature / Tool Overlay */}
             {toolState?.type === 'sign' && toolState.page === pageNumber && (
               <img 
                 src={toolState.url} 
                 alt="Signature Preview"
                 className="absolute pointer-events-none drop-shadow-lg opacity-80"
                 style={{
                   left: `${toolState.x}%`,
                   top: `${toolState.y}%`,
                   transform: `scale(${toolState.scale})`,
                   transformOrigin: 'top left',
                 }} 
               />
             )}
             
           </div>
        </Document>
      </div>
    </div>
  );
}
