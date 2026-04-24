import React, { useState } from 'react';
import { Sparkles, Wand2, Check, X, MessageSquare, Send } from 'lucide-react';
import { rewriteText, translateText, chatWithDocument } from '../services/api';

export default function RightPanel({ selectedBlock, handleApplyEdit, documentId, handleFixedExport, handleCancelEdit }) {
  const [instruction, setInstruction] = useState('');
  const [loading, setLoading] = useState(false);
  const [preview, setPreview] = useState(null);

  // Chat state
  const [chatInput, setChatInput] = useState('');
  const [chatMessages, setChatMessages] = useState([]);
  const [chatLoading, setChatLoading] = useState(false);

  const quickActions = ["Formalize", "Simplify", "Shorten"];
  const translationActions = ["English", "Indonesia", "Spanish"];

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

  const handleTranslate = async (lang) => {
    if (!selectedBlock) return;
    setLoading(true);
    try {
      const res = await translateText(selectedBlock.text, lang);
      // Mock preview object compatible with existing UI
      setPreview({
         edited_text: res.translatedText,
         short_reason: `Translated to ${lang}`
      });
    } catch (e) {
      console.error(e);
      alert("Failed to translate text.");
    } finally {
      setLoading(false);
    }
  };

  const handleSendChat = async () => {
    if (!chatInput.trim() || !documentId) return;

    const userMessage = { role: 'user', text: chatInput };
    setChatMessages(prev => [...prev, userMessage]);
    setChatInput('');
    setChatLoading(true);

    try {
      const res = await chatWithDocument(documentId, userMessage.text);
      setChatMessages(prev => [...prev, { role: 'ai', text: res.answer }]);
    } catch (e) {
      setChatMessages(prev => [...prev, { role: 'ai', text: "Error: Could not get answer." }]);
    } finally {
      setChatLoading(false);
    }
  };

  if (!selectedBlock) {
    return (
      <div className="w-80 bg-slate-50 border-l border-slate-200 flex flex-col h-full">
        {/* Chat UI */}
        <div className="p-4 border-b border-slate-200 bg-white flex items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <MessageSquare className="text-purple-600" size={20} />
            <h2 className="font-semibold text-slate-800">Chat with PDF</h2>
          </div>
          <button 
            onClick={handleFixedExport}
            className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-sm font-medium transition-colors"
            title="Export edited PDF"
          >
            Export PDF
          </button>
        </div>
        
        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-3">
          {chatMessages.length === 0 ? (
             <div className="text-center text-slate-400 mt-10">
                <Sparkles size={32} className="mx-auto mb-2 opacity-30" />
                <p className="text-sm">Ask me anything about this document!</p>
             </div>
          ) : (
            chatMessages.map((msg, idx) => (
              <div key={idx} className={`p-3 rounded-lg text-sm ${msg.role === 'user' ? 'bg-purple-100 text-purple-900 self-end ml-4' : 'bg-white border border-slate-200 text-slate-700 mr-4'}`}>
                {msg.text}
              </div>
            ))
          )}
          {chatLoading && (
             <div className="p-3 rounded-lg text-sm bg-white border border-slate-200 text-slate-400 mr-4 shadow-sm w-16 text-center">
                ... 
             </div>
          )}
        </div>

        <div className="p-4 bg-white border-t border-slate-200 flex gap-2">
           <input 
              type="text" 
              placeholder="Ask a question..."
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSendChat()}
              disabled={!documentId || chatLoading}
              className="flex-1 p-2 text-sm border rounded focus:outline-none focus:border-purple-300"
           />
           <button 
              onClick={handleSendChat}
              disabled={!documentId || chatLoading || !chatInput.trim()}
              className="bg-purple-600 text-white p-2 rounded hover:bg-purple-700 disabled:opacity-50"
           >
              <Send size={16} />
           </button>
        </div>
      </div>
    );
  }

  return (
    <div className="w-80 bg-slate-50 border-l border-slate-200 p-4 flex flex-col gap-6 overflow-y-auto">
      <div className="flex items-center justify-between border-b border-slate-200 pb-2">
        <h2 className="font-semibold text-slate-800 flex items-center gap-2">
          <Wand2 className="text-purple-600" size={18} /> Edit Block
        </h2>
        <button 
          onClick={handleCancelEdit}
          className="text-slate-400 hover:text-slate-700 hover:bg-slate-200 p-1 rounded transition-colors"
          title="Back to Chat"
        >
          <X size={18} />
        </button>
      </div>
      <div>
        <h3 className="font-semibold text-slate-800 mb-2">Original Text</h3>
        <div className="bg-white p-3 rounded border border-slate-200 text-sm text-slate-600">
          {selectedBlock.text}
        </div>
      </div>

      {!preview ? (
        <div className="flex flex-col gap-3">
          <h3 className="font-semibold text-slate-800 border-b pb-2">AI Rewrite</h3>
          <div className="flex flex-wrap gap-2 mb-2">
            {quickActions.map(act => (
              <button 
                key={act}
                onClick={() => handleRewrite(act)}
                className="text-xs bg-white border border-slate-200 px-2 py-1 rounded hover:bg-slate-100 font-medium"
              >
                {act}
              </button>
            ))}
          </div>
          
          <h3 className="font-semibold text-slate-800 border-b pb-2 mt-2">AI Translate</h3>
          <div className="flex flex-wrap gap-2 mb-2">
            {translationActions.map(lang => (
              <button 
                key={lang}
                onClick={() => handleTranslate(lang)}
                className="text-xs bg-blue-50 border border-blue-200 text-blue-700 px-2 py-1 rounded hover:bg-blue-100 font-medium"
              >
                {lang}
              </button>
            ))}
          </div>

          <div className="flex gap-2 mt-2">
            <input 
              type="text" 
              placeholder="Custom rewrite instruction..."
              value={instruction}
              onChange={(e) => setInstruction(e.target.value)}
              className="flex-1 p-2 text-sm border rounded"
            />
            <button 
              onClick={() => handleRewrite()}
              disabled={loading}
              className="bg-purple-600 text-white p-2 rounded hover:bg-purple-700 disabled:opacity-50 flex items-center justify-center min-w-[40px]"
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
             <p className="text-slate-800 whitespace-pre-wrap">{preview.edited_text}</p>
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
