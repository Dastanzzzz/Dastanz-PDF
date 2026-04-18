import React, { useRef, useState, useEffect } from 'react';
import { pdfjs } from 'react-pdf';
import { Type, Bold, Italic, Underline, ChevronDown, Download, ZoomIn, ZoomOut, ChevronLeft, ChevronRight } from 'lucide-react';

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString();

export default function FluidEditor({ content, onChange, onExport, file }) {
  const editorRef = useRef(null);
  const [isEditing, setIsEditing] = useState(false);
  const [showFontSizeMenu, setShowFontSizeMenu] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [zoom, setZoom] = useState(120);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const selectionRef = useRef(null);
  const menuRef = useRef(null);
  const isUserEditingRef = useRef(false);
  const initialLoadRef = useRef(false);

  const fontSizes = [8, 10, 12, 14, 16, 18, 20, 24, 28, 32];
  const [pageSize, setPageSize] = useState({ width: 595, height: 842 });
  const pageWidth = pageSize.width;
  const pageHeight = pageSize.height;
  const pagePadding = 0;
  const pageGap = 0;
  const columnWidth = pageWidth - pagePadding * 2;
  const zoomScale = zoom / 100;

  useEffect(() => {
    if (!file) return;

    let cancelled = false;
    const loadPageSize = async () => {
      try {
        const doc = await pdfjs.getDocument(file).promise;
        const page = await doc.getPage(1);
        const viewport = page.getViewport({ scale: 1 });
        if (!cancelled) {
          setPageSize({ width: viewport.width, height: viewport.height });
        }
      } catch (error) {
        console.warn('Failed to load PDF page size:', error);
      }
    };

    loadPageSize();
    return () => {
      cancelled = true;
    };
  }, [file]);

  useEffect(() => {
    if (!editorRef.current || isUserEditingRef.current) return;

    const incoming = content ?? '';
    if (!initialLoadRef.current || editorRef.current.innerHTML !== incoming) {
      editorRef.current.innerHTML = incoming;
      initialLoadRef.current = true;
    }
  }, [content]);

  useEffect(() => {
    if (!editorRef.current) return;
    const pages = editorRef.current.querySelectorAll('.pdf-page-content');
    pages.forEach(page => {
      if (pageHeight > 0) {
        page.style.height = `${pageHeight}px`;
        page.style.width = `${pageWidth}px`;
        page.style.position = 'relative';
        page.style.overflow = 'hidden';
        page.style.backgroundColor = 'white';
      }
    });
  }, [pageHeight, pageWidth, content]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setShowFontSizeMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (editorRef.current) {
        const pages = editorRef.current.querySelectorAll('.pdf-page-content');
        if (pages.length > 0) {
          setTotalPages(pages.length);
        } else {
          setTotalPages(1);
        }
      }
    }, 100);
    return () => clearTimeout(timer);
  }, [content]);

  useEffect(() => {
    if (!editorRef.current) return;
    const pages = editorRef.current.querySelectorAll('.pdf-page-content');
    if (pages[currentPage - 1]) {
      pages[currentPage - 1].scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, [currentPage]);

  const handleInput = (e) => {
    isUserEditingRef.current = true;
    onChange(e.currentTarget.innerHTML);
    // Reset flag after a short delay to allow for external updates
    setTimeout(() => {
      isUserEditingRef.current = false;
    }, 100);
  };

  const saveSelection = () => {
    const selection = window.getSelection();
    if (selection.rangeCount > 0) {
      selectionRef.current = selection.getRangeAt(0).cloneRange();
    }
  };

  const restoreSelection = () => {
    const selection = window.getSelection();
    if (selectionRef.current) {
      try {
        selection.removeAllRanges();
        selection.addRange(selectionRef.current.cloneRange());
      } catch (e) {
        console.log('Could not restore selection');
      }
    }
  };

  const applyFormat = (command, value = null) => {
    restoreSelection();
    document.execCommand(command, false, value);
    onChange(editorRef.current?.innerHTML);
    editorRef.current?.focus();
    saveSelection();
  };

  const applyFontSize = (size) => {
    restoreSelection();
    const selection = window.getSelection();
    
    if (selection.toString().length > 0) {
      const span = document.createElement('span');
      span.style.fontSize = size + 'px';
      
      try {
        const range = selection.getRangeAt(0);
        range.surroundContents(span);
      } catch (e) {
        const range = selection.getRangeAt(0);
        span.appendChild(range.extractContents());
        range.insertNode(span);
      }
    }
    
    onChange(editorRef.current?.innerHTML);
    editorRef.current?.focus();
    saveSelection();
  };

  const handleExportClick = async () => {
    setIsExporting(true);
    try {
      if (onExport) {
        await onExport(editorRef.current?.innerHTML);
      }
    } finally {
      setIsExporting(false);
    }
  };

  const handleZoomIn = () => {
    setZoom(Math.min(zoom + 10, 200));
  };

  const handleZoomOut = () => {
    setZoom(Math.max(zoom - 10, 50));
  };

  const handlePrevPage = () => {
    setCurrentPage(Math.max(currentPage - 1, 1));
  };

  const handleNextPage = () => {
    setCurrentPage(Math.min(currentPage + 1, totalPages));
  };

  return (
    <div className="flex-1 bg-white flex flex-col overflow-hidden">
      <div className="bg-slate-50 border-b border-slate-200 p-3 flex items-center justify-between">
        <div className="flex items-center gap-2 text-slate-600 font-medium">
          <Type size={18} />
          <span>Full Document Editor (Reflowable)</span>
        </div>
        <button
          onClick={handleExportClick}
          disabled={isExporting}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded text-sm font-medium transition"
          title="Export as PDF"
        >
          <Download size={16} />
          {isExporting ? 'Exporting...' : 'Export PDF'}
        </button>
      </div>

      <div className="bg-slate-100 border-b border-slate-200 p-2 flex items-center gap-2 flex-wrap">
        <button
          onMouseDown={(e) => { e.preventDefault(); saveSelection(); }}
          onClick={() => applyFormat('bold')}
          className="p-2 hover:bg-slate-200 rounded transition text-slate-700 font-bold"
          title="Bold"
        >
          <Bold size={18} />
        </button>
        
        <button
          onMouseDown={(e) => { e.preventDefault(); saveSelection(); }}
          onClick={() => applyFormat('italic')}
          className="p-2 hover:bg-slate-200 rounded transition text-slate-700 italic"
          title="Italic"
        >
          <Italic size={18} />
        </button>
        
        <button
          onMouseDown={(e) => { e.preventDefault(); saveSelection(); }}
          onClick={() => applyFormat('underline')}
          className="p-2 hover:bg-slate-200 rounded transition text-slate-700 underline"
          title="Underline"
        >
          <Underline size={18} />
        </button>

        <div className="border-l border-slate-300 h-6"></div>

        <div className="relative" ref={menuRef}>
          <button
            onMouseDown={(e) => { e.preventDefault(); saveSelection(); }}
            onClick={() => setShowFontSizeMenu(!showFontSizeMenu)}
            className="p-2 hover:bg-slate-200 rounded transition flex items-center gap-1 text-slate-700 text-sm font-medium"
            title="Font Size"
          >
            <span>A</span>
            <ChevronDown size={16} />
          </button>
          
          {showFontSizeMenu && (
            <div className="absolute top-full left-0 mt-1 bg-white border border-slate-300 rounded shadow-lg z-50 min-w-40">
              {fontSizes.map((size) => (
                <button
                  key={size}
                  onMouseDown={(e) => { e.preventDefault(); saveSelection(); }}
                  onClick={() => {
                    applyFontSize(size);
                    setShowFontSizeMenu(false);
                  }}
                  className="block w-full text-left px-4 py-2 hover:bg-blue-50 text-slate-700 transition border-b border-slate-100 last:border-b-0"
                  style={{ fontSize: size + 'px' }}
                >
                  {size}px
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="flex-1"></div>

        <div className="flex items-center gap-2 border-l border-slate-300 pl-2 pr-2">
          <button
            onClick={handlePrevPage}
            disabled={currentPage <= 1}
            className="p-2 hover:bg-slate-200 disabled:opacity-50 rounded transition text-slate-700"
            title="Previous Page"
          >
            <ChevronLeft size={18} />
          </button>
          <span className="text-sm font-medium text-slate-600 min-w-20 text-center">
            Page {currentPage} of {totalPages}
          </span>
          <button
            onClick={handleNextPage}
            disabled={currentPage >= totalPages}
            className="p-2 hover:bg-slate-200 disabled:opacity-50 rounded transition text-slate-700"
            title="Next Page"
          >
            <ChevronRight size={18} />
          </button>
        </div>

        <div className="flex items-center gap-2 border-l border-slate-300 pl-2">
          <button
            onClick={handleZoomOut}
            className="p-2 hover:bg-slate-200 rounded transition text-slate-700"
            title="Zoom Out"
          >
            <ZoomOut size={18} />
          </button>
          <span className="text-sm font-medium text-slate-600 min-w-12 text-center">{zoom}%</span>
          <button
            onClick={handleZoomIn}
            className="p-2 hover:bg-slate-200 rounded transition text-slate-700"
            title="Zoom In"
          >
            <ZoomIn size={18} />
          </button>
        </div>
      </div>
      
      <div className="flex-1 overflow-auto flex flex-col items-center pb-12 relative bg-slate-100 p-4 pt-10">
        <div
          style={{
            width: pageWidth * zoomScale,
            transformOrigin: 'top center'
          }}
        >
          <div
            ref={editorRef}
            className="outline-none text-slate-900 fluid-editor pb-20"
            contentEditable
            suppressContentEditableWarning
            onInput={handleInput}
            onFocus={() => setIsEditing(true)}
            onBlur={() => { setIsEditing(false); saveSelection(); }}
            onMouseUp={() => saveSelection()}
            onKeyUp={() => saveSelection()}
            style={{
              whiteSpace: 'normal',
              wordWrap: 'break-word',
              overflowWrap: 'break-word',
              textAlign: 'left',
              width: '100%',
              boxSizing: 'border-box',
              transform: `scale(${zoomScale})`,
              transformOrigin: 'top center'
            }}
          />
        </div>
      </div>
    </div>
  );
}
