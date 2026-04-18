import React from 'react';
import { FileText, Save, Info } from 'lucide-react';

export default function Sidebar({ documentId, handleExport, editMode, onModeChange }) {
  return (
    <div className="w-64 bg-slate-900 text-slate-300 flex flex-col p-4">
      <div className="flex items-center gap-2 mb-8 text-white font-bold text-xl">
        <FileText />
        <span>AI PDF Editor</span>
      </div>
      
      {documentId ? (
        <div className="flex flex-col gap-4 flex-1">
          <div className="bg-slate-800 p-3 rounded text-sm mb-4">
            <span className="block text-slate-500 mb-1">Session ID</span>
            <span className="font-mono text-xs">{documentId}</span>
          </div>
          
          <div className="flex flex-col gap-2 mb-6">
            <span className="text-xs uppercase font-bold text-slate-500 mb-1">Editor Mode</span>
            <button 
              onClick={() => onModeChange('fixed')}
              className={`w-full py-2 px-3 rounded text-sm text-left transition-colors flex items-center justify-between ${editMode === 'fixed' ? 'bg-blue-600 text-white' : 'hover:bg-slate-800'}`}
            >
              Block Edit (Original)
            </button>
            <button 
              onClick={() => onModeChange('fluid')}
              className={`w-full py-2 px-3 rounded text-sm text-left transition-colors flex items-center justify-between ${editMode === 'fluid' ? 'bg-blue-600 text-white' : 'hover:bg-slate-800'}`}
            >
              Full Edit (Reflowable)
            </button>
          </div>
          
          <div className="mt-auto">
            <button 
              onClick={handleExport}
              className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded flex items-center justify-center gap-2"
            >
              <Save size={16} />
              Export PDF
            </button>
            <div className="mt-4 flex items-start gap-2 text-xs text-slate-400">
               <Info size={14} className="flex-shrink-0" />
               <p>Regenerated PDFs are built dynamically. Formatting might slightly differ.</p>
            </div>
          </div>
        </div>
      ) : (
        <div className="text-sm text-slate-500">
          Upload a digital PDF to begin.
        </div>
      )}
    </div>
  );
}
