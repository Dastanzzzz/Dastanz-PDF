import React from 'react';
import { FileText, Save, Info, Minimize2, Lock, Merge, Droplets, Stamp, Scissors, LayoutGrid, FileImage, GitCompareArrows, EyeOff, ScanSearch, PenTool } from 'lucide-react';

const tools = [
  { id: 'compress',  label: 'Compress',      icon: Minimize2, activeClass: 'bg-blue-600/20 text-blue-400 border border-blue-500/30' },
  { id: 'password',  label: 'Add Password',  icon: Lock,      activeClass: 'bg-amber-600/20 text-amber-400 border border-amber-500/30' },
  { id: 'merge',     label: 'Merge PDFs',    icon: Merge,     activeClass: 'bg-green-600/20 text-green-400 border border-green-500/30' },
  { id: 'watermark', label: 'Watermark',     icon: Droplets,  activeClass: 'bg-cyan-600/20 text-cyan-400 border border-cyan-500/30' },
  { id: 'stamp',     label: 'Add Stamp',     icon: Stamp,     activeClass: 'bg-purple-600/20 text-purple-400 border border-purple-500/30' },
  { id: 'split',     label: 'Split PDF',     icon: Scissors,  activeClass: 'bg-rose-600/20 text-rose-400 border border-rose-500/30' },
  { id: 'arrange',   label: 'Arrange',       icon: LayoutGrid, activeClass: 'bg-teal-600/20 text-teal-400 border border-teal-500/30' },
  { id: 'convert',   label: 'Convert',       icon: FileImage, activeClass: 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30' },
  { id: 'compare',   label: 'Compare',       icon: GitCompareArrows, activeClass: 'bg-orange-600/20 text-orange-400 border border-orange-500/30' },
  { id: 'redact',    label: 'Redact',        icon: EyeOff,    activeClass: 'bg-red-600/20 text-red-400 border border-red-500/30' },
  { id: 'ocr',       label: 'OCR Text',      icon: ScanSearch, activeClass: 'bg-emerald-600/20 text-emerald-400 border border-emerald-500/30' },
  { id: 'sign',      label: 'Sign PDF',      icon: PenTool,   activeClass: 'bg-indigo-600/20 text-indigo-400 border border-indigo-500/30' },
];

export default function Sidebar({ documentId, editMode, onModeChange, selectedTool, onToolSelect }) {
  return (
    <div className="w-64 bg-slate-900 text-slate-300 flex flex-col p-4">
      <div className="flex items-center gap-2 mb-8 text-white font-bold text-xl">
        <FileText />
        <span>AI PDF Editor</span>
      </div>
      
      {documentId ? (
        <div className="flex flex-col gap-4 flex-1 min-h-0">
          <div className="bg-slate-800 p-3 rounded text-sm shrink-0">
            <span className="block text-slate-500 mb-1">Session ID</span>
            <span className="font-mono text-xs">{documentId}</span>
          </div>
          
          <div className="flex flex-col gap-2 shrink-0">
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

          {/* ─── PDF Tools Section ─── */}
          <div className="flex flex-col gap-2 flex-1 overflow-y-auto pr-2 custom-scrollbar">
            <span className="text-xs uppercase font-bold text-slate-500 mb-1 sticky top-0 bg-slate-900 z-10 py-1">PDF Tools</span>
            {tools.map(tool => {
              const Icon = tool.icon;
              const isActive = selectedTool === tool.id;
              return (
                <button
                  key={tool.id}
                  onClick={() => onToolSelect?.(isActive ? null : tool.id)}
                  className={`w-full py-2 px-3 rounded text-sm text-left transition-colors flex items-center gap-2 ${
                    isActive
                      ? tool.activeClass
                      : 'hover:bg-slate-800 border border-transparent'
                  }`}
                >
                  <Icon size={15} />
                  {tool.label}
                </button>
              );
            })}
          </div>
          
          <div className="mt-auto pt-4 border-t border-slate-800">
            <div className="flex items-start gap-2 text-xs text-slate-400">
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
