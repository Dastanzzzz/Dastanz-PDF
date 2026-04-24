import React, { useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

// Use local worker for Vite compatibility with react-pdf v9+
pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString();

export default function PdfViewer({ file, textBlocks, appliedEdits, onSelectBlock, toolState, setToolState }) {
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
                    onClick={(e) => {
                      if (toolState?.active) return;
                      onSelectBlock(block);
                    }}
                    className={`absolute cursor-pointer transition-all flex items-center justify-start overflow-hidden ${toolState?.active ? 'pointer-events-none' : 'pointer-events-auto'
                      } ${edit ? 'bg-white z-10' : 'border-2 border-blue-400/60 hover:bg-blue-400/20 hover:border-blue-600'
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

            {/* Insert Image Tool Overlay */}
            {toolState?.type === 'insertImage' && toolState.page === pageNumber && (
              <img
                src={toolState.url}
                alt="Insert Image Preview"
                className="absolute pointer-events-none drop-shadow border border-dashed border-pink-500 opacity-90"
                style={{
                  left: `${toolState.x}%`,
                  top: `${toolState.y}%`,
                  transform: `scale(${toolState.scale})`,
                  transformOrigin: 'top left',
                }}
              />
            )}

            {/* Draw Tool Overlay */}
            {toolState?.type === 'draw' && (
              <DrawOverlay
                toolState={toolState}
                setToolState={setToolState}
                scale={scale}
                pageNumber={pageNumber}
              />
            )}

            {/* Redact Tool Overlay */}
            {toolState?.type === 'redact' && (
              <RedactOverlay
                toolState={toolState}
                setToolState={setToolState}
                scale={scale}
                pageNumber={pageNumber}
              />
            )}

          </div>
        </Document>
      </div>
    </div>
  );
}

function DrawOverlay({ toolState, setToolState, scale, pageNumber }) {
  const svgRef = React.useRef(null);
  const [isDrawing, setIsDrawing] = React.useState(false);
  const [currentStroke, setCurrentStroke] = React.useState(null);

  React.useEffect(() => {
    if (svgRef.current && setToolState && toolState) {
      if (!toolState.clientPageWidth || !toolState.clientPageHeight) {
        setToolState(prev => ({
          ...prev,
          clientPageWidth: svgRef.current.clientWidth / scale,
          clientPageHeight: svgRef.current.clientHeight / scale
        }));
      }
    }
  }, [scale, toolState, setToolState]);

  const getCoordinates = (e) => {
    const rect = svgRef.current.getBoundingClientRect();
    return {
      x: (e.clientX - rect.left) / scale,
      y: (e.clientY - rect.top) / scale
    };
  };

  const handleMouseDown = (e) => {
    e.preventDefault();
    setIsDrawing(true);
    setCurrentStroke({
      page: pageNumber,
      color: toolState.color,
      thickness: toolState.thickness,
      points: [getCoordinates(e)]
    });
  };

  const handleMouseMove = (e) => {
    if (!isDrawing || !currentStroke) return;
    setCurrentStroke(prev => ({
      ...prev,
      points: [...prev.points, getCoordinates(e)]
    }));
  };

  const handleMouseUp = () => {
    if (isDrawing && currentStroke && currentStroke.points.length > 1) {
      if (setToolState) {
        setToolState(prev => ({
          ...prev,
          strokes: [...(prev.strokes || []), currentStroke]
        }));
      }
    }
    setIsDrawing(false);
    setCurrentStroke(null);
  };

  // Filter strokes to only show ones for current page
  const pageStrokes = (toolState.strokes || []).filter(s => s.page === pageNumber);
  if (currentStroke && currentStroke.page === pageNumber) {
    pageStrokes.push(currentStroke);
  }

  return (
    <svg
      ref={svgRef}
      className="absolute top-0 left-0 w-full h-full cursor-crosshair"
      style={{ zIndex: 100 }}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
    >
      <rect x="0" y="0" width="100%" height="100%" fill="transparent" />
      {pageStrokes.map((stroke, i) => {
        const d = `M ${stroke.points.map(p => `${p.x * scale},${p.y * scale}`).join(' L ')}`;
        const isHighlight = stroke.color.toLowerCase() === '#fbbf24' || stroke.color.toLowerCase() === '#facc15';

        return (
          <path
            key={i}
            d={d}
            fill="none"
            stroke={stroke.color}
            strokeWidth={stroke.thickness * scale}
            strokeLinecap="round"
            strokeLinejoin="round"
            opacity={isHighlight && stroke.thickness > 10 ? 0.4 : 1}
          />
        );
      })}
    </svg>
  );
}


function DraggableResizableBox({ box, scale, updateBox, removeBox }) {
  const [dragState, setDragState] = React.useState(null);

  const handlePointerDown = (e, type, dir = null) => {
    e.stopPropagation();
    e.currentTarget.setPointerCapture(e.pointerId);
    setDragState({
      type,
      dir,
      startX: e.clientX,
      startY: e.clientY,
      initX: box.x,
      initY: box.y,
      initW: box.width,
      initH: box.height
    });
  };

  const handlePointerMove = (e) => {
    if (!dragState) return;
    e.stopPropagation();

    const dx = (e.clientX - dragState.startX) / scale;
    const dy = (e.clientY - dragState.startY) / scale;

    let newX = dragState.initX;
    let newY = dragState.initY;
    let newW = dragState.initW;
    let newH = dragState.initH;

    if (dragState.type === 'move') {
      newX += dx;
      newY += dy;
    } else if (dragState.type === 'resize') {
      const dir = dragState.dir;
      if (dir.includes('n')) {
        newY += dy;
        newH -= dy;
      }
      if (dir.includes('s')) {
        newH += dy;
      }
      if (dir.includes('w')) {
        newX += dx;
        newW -= dx;
      }
      if (dir.includes('e')) {
        newW += dx;
      }

      // Enforce minimum sizes logic properly
      if (newW < 20) {
        if (dir.includes('w')) newX -= (20 - newW);
        newW = 20;
      }
      if (newH < 20) {
        if (dir.includes('n')) newY -= (20 - newH);
        newH = 20;
      }
    }

    updateBox({ x: newX, y: newY, width: newW, height: newH });
  };

  const handlePointerUp = (e) => {
    if (dragState) {
      e.currentTarget.releasePointerCapture(e.pointerId);
      setDragState(null);
    }
  };

  const handleStyle = { position: 'absolute', backgroundColor: 'rgba(255, 255, 255, 0.8)', border: '1px solid #64748b' };
  const hSize = 10;

  return (
    <div
      className="absolute border shadow-md flex items-start justify-end group"
      style={{
        left: box.x * scale,
        top: box.y * scale,
        width: box.width * scale,
        height: box.height * scale,
        backgroundColor: box.color,
        borderColor: box.color.toLowerCase() === '#ffffff' ? '#ccc' : 'white',
        borderStyle: 'solid',
        borderWidth: '1px',
        cursor: dragState?.type === 'move' ? 'grabbing' : 'grab',
        pointerEvents: 'auto',
      }}
      onPointerDown={(e) => handlePointerDown(e, 'move')}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerCancel={handlePointerUp}
    >
      <button
        onClick={(e) => { e.stopPropagation(); removeBox(); }}
        className="text-white hover:text-red-400 p-1 opacity-0 group-hover:opacity-100 bg-black/30 rounded-bl-md transition-opacity pointer-events-auto z-10"
        title="Remove"
        onPointerDown={(e) => e.stopPropagation()}
      >
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3"><path d="M18 6L6 18M6 6l12 12"></path></svg>
      </button>

      {/* Resize Handles */}
      {/* N */}
      <div style={{ ...handleStyle, top: -hSize / 2, left: '50%', width: hSize, height: hSize, marginLeft: -hSize / 2, cursor: 'n-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'n')} />
      {/* S */}
      <div style={{ ...handleStyle, bottom: -hSize / 2, left: '50%', width: hSize, height: hSize, marginLeft: -hSize / 2, cursor: 's-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 's')} />
      {/* E */}
      <div style={{ ...handleStyle, top: '50%', right: -hSize / 2, width: hSize, height: hSize, marginTop: -hSize / 2, cursor: 'e-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'e')} />
      {/* W */}
      <div style={{ ...handleStyle, top: '50%', left: -hSize / 2, width: hSize, height: hSize, marginTop: -hSize / 2, cursor: 'w-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'w')} />

      {/* NE */}
      <div style={{ ...handleStyle, top: -hSize / 2, right: -hSize / 2, width: hSize, height: hSize, cursor: 'ne-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'ne')} />
      {/* NW */}
      <div style={{ ...handleStyle, top: -hSize / 2, left: -hSize / 2, width: hSize, height: hSize, cursor: 'nw-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'nw')} />
      {/* SE */}
      <div style={{ ...handleStyle, bottom: -hSize / 2, right: -hSize / 2, width: hSize, height: hSize, cursor: 'se-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'se')} />
      {/* SW */}
      <div style={{ ...handleStyle, bottom: -hSize / 2, left: -hSize / 2, width: hSize, height: hSize, cursor: 'sw-resize' }} onPointerDown={(e) => handlePointerDown(e, 'resize', 'sw')} />
    </div>
  );
}

function RedactOverlay({ toolState, setToolState, scale, pageNumber }) {
  const containerRef = React.useRef(null);

  React.useEffect(() => {
    if (containerRef.current && setToolState && toolState) {
      if (!toolState.clientPageWidth || !toolState.clientPageHeight) {
        setToolState(prev => ({
          ...prev,
          clientPageWidth: containerRef.current.clientWidth / scale,
          clientPageHeight: containerRef.current.clientHeight / scale
        }));
      }
    }
  }, [scale, toolState, setToolState]);

  const updateBox = (id, newProps) => {
    if (setToolState) {
      setToolState(prev => ({
        ...prev,
        boxes: (prev.boxes || []).map(b => (b.id === id ? { ...b, ...newProps } : b))
      }));
    }
  };

  const removeBox = (id) => {
    if (setToolState) {
      setToolState(prev => ({
        ...prev,
        boxes: (prev.boxes || []).filter(b => b.id !== id)
      }));
    }
  };

  return (
    <div
      ref={containerRef}
      className="absolute top-0 left-0 w-full h-full pointer-events-none"
      style={{ zIndex: 100 }}
    >
      {(toolState.boxes || []).filter(b => b.page === pageNumber).map((box) => (
        <DraggableResizableBox
          key={box.id}
          box={box}
          scale={scale}
          updateBox={(props) => updateBox(box.id, props)}
          removeBox={() => removeBox(box.id)}
        />
      ))}
    </div>
  );
}
