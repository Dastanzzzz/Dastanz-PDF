import React, { useState } from 'react';
import { Sparkles, Wand2, Check, X } from 'lucide-react';
import { rewriteText } from '../services/api';

export default function RightPanel({ selectedBlock, handleApplyEdit, handleCancelEdit }) {
  const [instruction, setInstruction] = useState('');
  const [loading, setLoading] = useState(false);
  const [preview, setPreview] = useState(null);

  const quickActions = ["Formalize", "Simplify", "Shorten", "Translate to English"];

  const handleRewrite = async (overrideInstruction = null) => {
    if (!selectedBlock) return;
    const prompt = overrideInstruction || instruction;
    if (!prompt.trim()) return;

    setLoading(true);
    try {
      const res = await rewriteText(selectedBlock.text, prompt);
      setPreview(res);
    } catch (e) {
      console.error(e);
      alert("Failed to rewrite text.");
    } finally {
      setLoading(false);
    }
  };

  if (!selectedBlock) {
    return (
      <div className="w-80 bg-slate-50 border-l border-slate-200 p-6 flex flex-col items-center justify-center text-slate-400">
        <Sparkles size={48} className="mb-4 opacity-20" />
        <p className="text-center">Select a text block in the PDF to start editing with AI.</p>
      </div>
    );
  }

  return (
    <div className="w-80 bg-slate-50 border-l border-slate-200 p-4 flex flex-col gap-6 overflow-y-auto">
      <div>
        <h3 className="font-semibold text-slate-800 mb-2">Original Text</h3>
        <div className="bg-white p-3 rounded border border-slate-200 text-sm text-slate-600">
          {selectedBlock.text}
        </div>
      </div>

      {!preview ? (
        <div className="flex flex-col gap-3">
          <h3 className="font-semibold text-slate-800 border-b pb-2">AI Tools</h3>
          <div className="flex flex-wrap gap-2 mb-2">
            {quickActions.map(act => (
              <button 
                key={act}
                onClick={() => handleRewrite(act)}
                className="text-xs bg-white border border-slate-200 px-2 py-1 rounded hover:bg-slate-100"
              >
                {act}
              </button>
            ))}
          </div>
          <div className="flex gap-2">
            <input 
              type="text" 
              placeholder="Custom instruction..."
              value={instruction}
              onChange={(e) => setInstruction(e.target.value)}
              className="flex-1 p-2 text-sm border rounded"
            />
            <button 
              onClick={() => handleRewrite()}
              disabled={loading}
              className="bg-purple-600 text-white p-2 rounded hover:bg-purple-700 disabled:opacity-50 flex items-center justify-center"
            >
              <Wand2 size={16} />
            </button>
          </div>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
           <h3 className="font-semibold text-slate-800 border-b pb-2">Diff Preview</h3>
           <div className="bg-white p-3 rounded border border-purple-200 shadow-sm text-sm">
             <div className="mb-2"><strong>Reason:</strong> <span className="text-slate-600">{preview.short_reason}</span></div>
             <p className="text-slate-800">{preview.edited_text}</p>
           </div>
           
           <div className="flex gap-2 mt-2">
             <button onClick={() => {
                handleApplyEdit({ ...selectedBlock, newText: preview.edited_text });
                setPreview(null);
                setInstruction('');
             }} className="flex-1 bg-green-600 text-white py-2 rounded flex items-center justify-center gap-2 text-sm hover:bg-green-700">
               <Check size={16} /> Accept
             </button>
             <button onClick={() => setPreview(null)} className="flex-1 bg-white border border-slate-300 py-2 rounded flex items-center justify-center gap-2 text-sm text-slate-600 hover:bg-slate-50">
               <X size={16} /> Reject
             </button>
           </div>
        </div>
      )}
    </div>
  );
}
