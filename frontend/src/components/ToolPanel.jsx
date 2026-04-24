import React, { useState, useRef } from 'react';
import { 
  Minimize2, Lock, Merge, Droplets, Stamp, Download, 
  X, UploadCloud, GripVertical, AlertCircle, Check, Loader2,
  Scissors, LayoutGrid, FileImage, GitCompareArrows,
  RotateCw, RotateCcw, Trash2, Copy, ChevronUp, ChevronDown,
  EyeOff, ScanSearch, PenTool, CheckCircle, Edit3, ImagePlus
} from 'lucide-react';
import { compressPdf, addPassword, mergePdfs, addWatermark, addStamp, splitPdf, arrangePdf, convertPdf, comparePdfs, redactPdf, ocrPdf, signPdf, drawOnPdf, insertImageOnPdf } from '../services/toolsApi';

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  setTimeout(() => {
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }, 100);
}

// ─── Compress Tool ─────────────────────────────────────────────

function CompressTool({ documentId }) {
  const [quality, setQuality] = useState('medium');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const handleCompress = async () => {
    if (!documentId) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await compressPdf(documentId, quality);
      downloadBlob(blob, 'compressed.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Compression failed: ' + (e.response?.data?.message || e.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-blue-400 mb-1">
        <Minimize2 size={20} />
        <h3 className="font-semibold text-white text-base">Compress PDF</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Reduce file size by recompressing embedded images.</p>
      
      <div>
        <label className="block text-xs font-medium text-slate-400 mb-2">Compression Level</label>
        <div className="flex gap-2">
          {['low', 'medium', 'high'].map(q => (
            <button
              key={q}
              onClick={() => setQuality(q)}
              className={`flex-1 py-2 px-3 rounded text-xs font-medium transition-all ${
                quality === q 
                  ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30' 
                  : 'bg-slate-700/60 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {q === 'low' ? '🔥 Max' : q === 'medium' ? '⚖️ Balanced' : '✨ Light'}
            </button>
          ))}
        </div>
        <p className="text-slate-500 text-[10px] mt-1.5">
          {quality === 'low' ? 'Smallest size, lower image quality' : 
           quality === 'medium' ? 'Good balance of size and quality' : 
           'Best quality, moderate size reduction'}
        </p>
      </div>

      <button
        onClick={handleCompress}
        disabled={loading || !documentId}
        className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-blue-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Compressing...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><Download size={16} /> Compress & Download</>}
      </button>
    </div>
  );
}

// ─── Password Tool ─────────────────────────────────────────────

function PasswordTool({ documentId }) {
  const [userPassword, setUserPassword] = useState('');
  const [ownerPassword, setOwnerPassword] = useState('');
  const [allowPrint, setAllowPrint] = useState(true);
  const [allowCopy, setAllowCopy] = useState(false);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const handleProtect = async () => {
    if (!documentId || !userPassword) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await addPassword(documentId, userPassword, ownerPassword, allowPrint, allowCopy);
      downloadBlob(blob, 'protected.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Password protection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-amber-400 mb-1">
        <Lock size={20} />
        <h3 className="font-semibold text-white text-base">Add Password</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Encrypt your PDF so it requires a password to open.</p>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Open Password *</label>
        <input
          type="password"
          value={userPassword}
          onChange={(e) => setUserPassword(e.target.value)}
          placeholder="Required to open the PDF"
          className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Owner Password (optional)</label>
        <input
          type="password"
          value={ownerPassword}
          onChange={(e) => setOwnerPassword(e.target.value)}
          placeholder="For admin access"
          className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500/30"
        />
      </div>

      <div className="flex flex-col gap-2">
        <label className="block text-xs font-medium text-slate-400 mb-1">Permissions</label>
        <label className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer">
          <input type="checkbox" checked={allowPrint} onChange={(e) => setAllowPrint(e.target.checked)} className="rounded accent-blue-500" />
          Allow printing
        </label>
        <label className="flex items-center gap-2 text-sm text-slate-300 cursor-pointer">
          <input type="checkbox" checked={allowCopy} onChange={(e) => setAllowCopy(e.target.checked)} className="rounded accent-blue-500" />
          Allow copying text
        </label>
      </div>

      <button
        onClick={handleProtect}
        disabled={loading || !documentId || !userPassword}
        className="w-full py-2.5 bg-amber-600 hover:bg-amber-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-amber-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Encrypting...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><Lock size={16} /> Protect & Download</>}
      </button>
    </div>
  );
}

// ─── Merge Tool ────────────────────────────────────────────────

function MergeTool() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const fileInputRef = useRef(null);

  const handleAddFiles = (e) => {
    const newFiles = Array.from(e.target.files);
    setFiles(prev => [...prev, ...newFiles]);
    setDone(false);
    e.target.value = '';
  };

  const handleRemoveFile = (index) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
    setDone(false);
  };

  const moveFile = (from, to) => {
    if (to < 0 || to >= files.length) return;
    const updated = [...files];
    const [moved] = updated.splice(from, 1);
    updated.splice(to, 0, moved);
    setFiles(updated);
  };

  const handleMerge = async () => {
    if (files.length < 2) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await mergePdfs(files);
      downloadBlob(blob, 'merged.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Merge failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-green-400 mb-1">
        <Merge size={20} />
        <h3 className="font-semibold text-white text-base">Merge PDFs</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Combine multiple PDF files into one document.</p>

      <input
        ref={fileInputRef}
        type="file"
        accept="application/pdf"
        multiple
        onChange={handleAddFiles}
        className="hidden"
      />

      <button
        onClick={() => fileInputRef.current?.click()}
        className="w-full py-3 border-2 border-dashed border-slate-600 rounded-lg text-slate-400 hover:border-green-500 hover:text-green-400 transition-all flex items-center justify-center gap-2 text-sm"
      >
        <UploadCloud size={18} /> Add PDF Files
      </button>

      {files.length > 0 && (
        <div className="flex flex-col gap-1.5 max-h-48 overflow-y-auto">
          {files.map((file, i) => (
            <div key={i} className="flex items-center gap-2 bg-slate-700/40 rounded-lg px-3 py-2 text-xs">
              <div className="flex flex-col gap-0.5">
                <button onClick={() => moveFile(i, i - 1)} disabled={i === 0} className="text-slate-500 hover:text-white disabled:opacity-20 leading-none">▲</button>
                <button onClick={() => moveFile(i, i + 1)} disabled={i === files.length - 1} className="text-slate-500 hover:text-white disabled:opacity-20 leading-none">▼</button>
              </div>
              <span className="text-slate-300 flex-1 truncate">{file.name}</span>
              <span className="text-slate-500">{(file.size / 1024).toFixed(0)}KB</span>
              <button onClick={() => handleRemoveFile(i)} className="text-slate-500 hover:text-red-400">
                <X size={14} />
              </button>
            </div>
          ))}
        </div>
      )}

      <button
        onClick={handleMerge}
        disabled={loading || files.length < 2}
        className="w-full py-2.5 bg-green-600 hover:bg-green-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-green-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Merging...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><Merge size={16} /> Merge & Download</>}
      </button>

      {files.length > 0 && files.length < 2 && (
        <div className="flex items-start gap-1.5 text-amber-400/80 text-[10px]">
          <AlertCircle size={12} className="flex-shrink-0 mt-0.5" />
          <span>Add at least 2 files to merge.</span>
        </div>
      )}
    </div>
  );
}

// ─── Watermark Tool ────────────────────────────────────────────

function WatermarkTool({ documentId }) {
  const [text, setText] = useState('CONFIDENTIAL');
  const [fontSize, setFontSize] = useState(48);
  const [opacity, setOpacity] = useState(0.3);
  const [rotation, setRotation] = useState(45);
  const [color, setColor] = useState('#CCCCCC');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const handleApply = async () => {
    if (!documentId || !text) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await addWatermark(documentId, { text, fontSize, opacity, rotation, color });
      downloadBlob(blob, 'watermarked.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Watermark failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-cyan-400 mb-1">
        <Droplets size={20} />
        <h3 className="font-semibold text-white text-base">Add Watermark</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Overlay text on every page of your PDF.</p>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Watermark Text</label>
        <input
          type="text"
          value={text}
          onChange={(e) => setText(e.target.value)}
          className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500/30"
        />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1.5">Font Size</label>
          <input
            type="number"
            value={fontSize}
            onChange={(e) => setFontSize(Number(e.target.value))}
            min={8} max={200}
            className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
          />
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1.5">Rotation °</label>
          <input
            type="number"
            value={rotation}
            onChange={(e) => setRotation(Number(e.target.value))}
            min={-180} max={180}
            className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-cyan-500"
          />
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">
          Opacity: <span className="text-cyan-400">{(opacity * 100).toFixed(0)}%</span>
        </label>
        <input
          type="range"
          value={opacity}
          onChange={(e) => setOpacity(Number(e.target.value))}
          min={0.05} max={1} step={0.05}
          className="w-full accent-cyan-500"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Color</label>
        <div className="flex items-center gap-2">
          <input
            type="color"
            value={color}
            onChange={(e) => setColor(e.target.value)}
            className="w-10 h-8 rounded border border-slate-600 cursor-pointer bg-transparent"
          />
          <span className="text-xs text-slate-400 font-mono">{color}</span>
        </div>
      </div>

      <button
        onClick={handleApply}
        disabled={loading || !documentId || !text}
        className="w-full py-2.5 bg-cyan-600 hover:bg-cyan-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-cyan-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Applying...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><Droplets size={16} /> Apply & Download</>}
      </button>
    </div>
  );
}

// ─── Stamp Tool ────────────────────────────────────────────────

function StampTool({ documentId }) {
  const [stampFile, setStampFile] = useState(null);
  const [position, setPosition] = useState('center');
  const [scale, setScale] = useState(0.5);
  const [opacity, setOpacity] = useState(0.8);
  const [pageSelection, setPageSelection] = useState('all');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const fileInputRef = useRef(null);

  const handleApply = async () => {
    if (!documentId || !stampFile) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await addStamp(documentId, stampFile, { position, scale, opacity, pageSelection });
      downloadBlob(blob, 'stamped.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Stamp failed.');
    } finally {
      setLoading(false);
    }
  };

  const positions = [
    { value: 'center', label: '⊕ Center' },
    { value: 'top-left', label: '↖ Top Left' },
    { value: 'top-right', label: '↗ Top Right' },
    { value: 'bottom-left', label: '↙ Bottom Left' },
    { value: 'bottom-right', label: '↘ Bottom Right' },
  ];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-purple-400 mb-1">
        <Stamp size={20} />
        <h3 className="font-semibold text-white text-base">Add Stamp</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Place an image stamp on your PDF pages.</p>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/png,image/jpeg,image/jpg"
        onChange={(e) => { setStampFile(e.target.files[0]); setDone(false); }}
        className="hidden"
      />

      <button
        onClick={() => fileInputRef.current?.click()}
        className="w-full py-3 border-2 border-dashed border-slate-600 rounded-lg text-slate-400 hover:border-purple-500 hover:text-purple-400 transition-all flex items-center justify-center gap-2 text-sm"
      >
        <UploadCloud size={18} />
        {stampFile ? stampFile.name : 'Choose Stamp Image (PNG/JPG)'}
      </button>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Position</label>
        <div className="grid grid-cols-2 gap-1.5">
          {positions.map(p => (
            <button
              key={p.value}
              onClick={() => setPosition(p.value)}
              className={`py-1.5 px-2 rounded text-xs transition-all ${
                position === p.value 
                  ? 'bg-purple-600 text-white' 
                  : 'bg-slate-700/60 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">
          Scale: <span className="text-purple-400">{(scale * 100).toFixed(0)}%</span>
        </label>
        <input
          type="range"
          value={scale}
          onChange={(e) => setScale(Number(e.target.value))}
          min={0.1} max={2} step={0.05}
          className="w-full accent-purple-500"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">
          Opacity: <span className="text-purple-400">{(opacity * 100).toFixed(0)}%</span>
        </label>
        <input
          type="range"
          value={opacity}
          onChange={(e) => setOpacity(Number(e.target.value))}
          min={0.05} max={1} step={0.05}
          className="w-full accent-purple-500"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Apply To</label>
        <div className="flex gap-2">
          {['all', 'first', 'last'].map(ps => (
            <button
              key={ps}
              onClick={() => setPageSelection(ps)}
              className={`flex-1 py-1.5 rounded text-xs font-medium transition-all ${
                pageSelection === ps 
                  ? 'bg-purple-600 text-white' 
                  : 'bg-slate-700/60 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {ps === 'all' ? 'All Pages' : ps === 'first' ? 'First Page' : 'Last Page'}
            </button>
          ))}
        </div>
      </div>

      <button
        onClick={handleApply}
        disabled={loading || !documentId || !stampFile}
        className="w-full py-2.5 bg-purple-600 hover:bg-purple-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-purple-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Stamping...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><Stamp size={16} /> Stamp & Download</>}
      </button>
    </div>
  );
}

// ─── Split Tool ────────────────────────────────────────────────

function SplitTool({ documentId }) {
  const [pages, setPages] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const handleSplit = async () => {
    if (!documentId || !pages.trim()) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await splitPdf(documentId, pages);
      downloadBlob(blob, 'split.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Split failed: ' + e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-rose-400 mb-1">
        <Scissors size={20} />
        <h3 className="font-semibold text-white text-base">Split PDF</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Extract specific pages from your PDF into a new document.</p>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Page Range</label>
        <input
          type="text"
          value={pages}
          onChange={(e) => { setPages(e.target.value); setDone(false); }}
          placeholder="e.g. 1-3, 5, 7-9"
          className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500/30"
        />
        <p className="text-slate-500 text-[10px] mt-1.5">Use commas to separate pages or ranges. Example: 1-3, 5, 8-10</p>
      </div>

      <button
        onClick={handleSplit}
        disabled={loading || !documentId || !pages.trim()}
        className="w-full py-2.5 bg-rose-600 hover:bg-rose-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-rose-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Splitting...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><Scissors size={16} /> Split & Download</>}
      </button>
    </div>
  );
}

// ─── Arrange Tool ──────────────────────────────────────────────

function ArrangeTool({ documentId }) {
  const [pageList, setPageList] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const handleSetPageCount = (count) => {
    const num = Math.max(1, Math.min(count, 500));
    setTotalPages(num);
    setPageList(Array.from({ length: num }, (_, i) => ({
      sourcePage: i,
      rotation: 0,
      deleted: false,
      id: `page-${i}-${Date.now()}`
    })));
    setDone(false);
  };

  const movePage = (from, to) => {
    if (to < 0 || to >= pageList.length) return;
    const updated = [...pageList];
    const [moved] = updated.splice(from, 1);
    updated.splice(to, 0, moved);
    setPageList(updated);
    setDone(false);
  };

  const rotatePage = (index, degrees) => {
    const updated = [...pageList];
    updated[index] = { ...updated[index], rotation: (updated[index].rotation + degrees + 360) % 360 };
    setPageList(updated);
    setDone(false);
  };

  const toggleDelete = (index) => {
    const updated = [...pageList];
    updated[index] = { ...updated[index], deleted: !updated[index].deleted };
    setPageList(updated);
    setDone(false);
  };

  const duplicatePage = (index) => {
    const updated = [...pageList];
    const dup = { ...updated[index], id: `page-${updated[index].sourcePage}-dup-${Date.now()}` };
    updated.splice(index + 1, 0, dup);
    setPageList(updated);
    setDone(false);
  };

  const handleArrange = async () => {
    if (!documentId) return;
    const activePages = pageList.filter(p => !p.deleted);
    if (activePages.length === 0) {
      alert('No pages selected. At least one page must remain.');
      return;
    }

    setLoading(true);
    setDone(false);
    try {
      const instructions = activePages.map(p => ({
        sourcePage: p.sourcePage,
        rotation: p.rotation
      }));
      const blob = await arrangePdf(documentId, instructions);
      downloadBlob(blob, 'arranged.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Arrange failed: ' + e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-teal-400 mb-1">
        <LayoutGrid size={20} />
        <h3 className="font-semibold text-white text-base">Arrange Pages</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Reorder, rotate, delete, or duplicate pages in your PDF.</p>

      {totalPages === 0 ? (
        <div>
          <label className="block text-xs font-medium text-slate-400 mb-1.5">Number of Pages in Document</label>
          <div className="flex gap-2">
            <input
              type="number"
              min={1}
              max={500}
              placeholder="Enter page count"
              className="flex-1 bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white placeholder:text-slate-500 focus:outline-none focus:border-teal-500 focus:ring-1 focus:ring-teal-500/30"
              onKeyDown={(e) => { if (e.key === 'Enter') handleSetPageCount(Number(e.target.value)); }}
            />
            <button
              onClick={(e) => {
                const input = e.target.closest('.flex').querySelector('input');
                handleSetPageCount(Number(input.value));
              }}
              className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg text-sm font-medium transition-all"
            >
              Set
            </button>
          </div>
          <p className="text-slate-500 text-[10px] mt-1.5">Enter the total number of pages to start arranging.</p>
        </div>
      ) : (
        <>
          <div className="flex flex-col gap-1.5 max-h-72 overflow-y-auto pr-1">
            {pageList.map((page, i) => (
              <div
                key={page.id}
                className={`flex items-center gap-1.5 rounded-lg px-2.5 py-2 text-xs transition-all ${
                  page.deleted
                    ? 'bg-red-900/20 border border-red-800/30 opacity-50'
                    : 'bg-slate-700/40 border border-transparent'
                }`}
              >
                {/* Move arrows */}
                <div className="flex flex-col gap-0.5">
                  <button onClick={() => movePage(i, i - 1)} disabled={i === 0} className="text-slate-500 hover:text-white disabled:opacity-20 leading-none">
                    <ChevronUp size={12} />
                  </button>
                  <button onClick={() => movePage(i, i + 1)} disabled={i === pageList.length - 1} className="text-slate-500 hover:text-white disabled:opacity-20 leading-none">
                    <ChevronDown size={12} />
                  </button>
                </div>

                {/* Page info */}
                <span className={`flex-1 font-medium ${page.deleted ? 'line-through text-red-400' : 'text-slate-300'}`}>
                  Page {page.sourcePage + 1}
                  {page.rotation !== 0 && <span className="text-teal-400 ml-1">↻{page.rotation}°</span>}
                </span>

                {/* Action buttons */}
                <button onClick={() => rotatePage(i, -90)} title="Rotate CCW" className="p-1 text-slate-500 hover:text-teal-400 transition-colors">
                  <RotateCcw size={12} />
                </button>
                <button onClick={() => rotatePage(i, 90)} title="Rotate CW" className="p-1 text-slate-500 hover:text-teal-400 transition-colors">
                  <RotateCw size={12} />
                </button>
                <button onClick={() => duplicatePage(i)} title="Duplicate" className="p-1 text-slate-500 hover:text-blue-400 transition-colors">
                  <Copy size={12} />
                </button>
                <button onClick={() => toggleDelete(i)} title={page.deleted ? 'Restore' : 'Delete'} className={`p-1 transition-colors ${page.deleted ? 'text-green-400 hover:text-green-300' : 'text-slate-500 hover:text-red-400'}`}>
                  <Trash2 size={12} />
                </button>
              </div>
            ))}
          </div>

          <div className="flex items-center gap-2 text-[10px] text-slate-500">
            <span>{pageList.filter(p => !p.deleted).length} of {pageList.length} pages active</span>
            <button
              onClick={() => { setTotalPages(0); setPageList([]); }}
              className="ml-auto text-slate-500 hover:text-white text-[10px] underline"
            >
              Reset
            </button>
          </div>

          <button
            onClick={handleArrange}
            disabled={loading || !documentId || pageList.filter(p => !p.deleted).length === 0}
            className="w-full py-2.5 bg-teal-600 hover:bg-teal-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-teal-600/20"
          >
            {loading ? <><Loader2 size={16} className="animate-spin" /> Arranging...</> :
             done ? <><Check size={16} /> Downloaded!</> :
             <><LayoutGrid size={16} /> Apply & Download</>}
          </button>
        </>
      )}
    </div>
  );
}

// ─── Convert Tool ──────────────────────────────────────────────

function ConvertTool({ documentId }) {
  const [format, setFormat] = useState('png');
  const [dpi, setDpi] = useState(150);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const isWordFormat = format === 'docx' || format === 'word';

  const handleConvert = async () => {
    if (!documentId) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await convertPdf(documentId, format, dpi);
      const mimeType = isWordFormat
        ? 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
        : 'application/zip';
      const filename = isWordFormat ? 'converted.docx' : `converted_${format}.zip`;
      const url = window.URL.createObjectURL(new Blob([blob], { type: mimeType }));
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      setTimeout(() => {
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      }, 100);
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Convert failed: ' + e.message);
    } finally {
      setLoading(false);
    }
  };

  const dpiPresets = [
    { value: 72, label: '72 (Screen)' },
    { value: 150, label: '150 (Default)' },
    { value: 300, label: '300 (Print)' },
  ];

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-indigo-400 mb-1">
        <FileImage size={20} />
        <h3 className="font-semibold text-white text-base">Convert PDF</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">
        {isWordFormat
          ? 'Convert PDF text content into a Word document (.docx).'
          : 'Convert each page of your PDF into image files. Downloads as a ZIP archive.'}
      </p>
      {isWordFormat && (
        <p className="text-amber-400/90 text-[10px] leading-relaxed">
          Best for text extraction. Complex layouts, tables, and graphics may not fully match the original PDF.
        </p>
      )}

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-2">Output Format</label>
        <div className="flex gap-2">
          {['png', 'jpeg', 'docx'].map(f => (
            <button
              key={f}
              onClick={() => { setFormat(f); setDone(false); }}
              className={`flex-1 py-2 px-3 rounded text-xs font-medium transition-all ${
                format === f
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : 'bg-slate-700/60 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {f === 'png' ? '🖼️ PNG (Lossless)' : f === 'jpeg' ? '📷 JPEG (Smaller)' : '📝 Word (.docx)'}
            </button>
          ))}
        </div>
      </div>

      {!isWordFormat && (
      <div>
        <label className="block text-xs font-medium text-slate-400 mb-2">Resolution (DPI)</label>
        <div className="flex gap-2">
          {dpiPresets.map(preset => (
            <button
              key={preset.value}
              onClick={() => { setDpi(preset.value); setDone(false); }}
              className={`flex-1 py-2 px-2 rounded text-xs font-medium transition-all ${
                dpi === preset.value
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                  : 'bg-slate-700/60 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {preset.label}
            </button>
          ))}
        </div>
        <p className="text-slate-500 text-[10px] mt-1.5">
          Higher DPI = better quality but larger file size
        </p>
      </div>
      )}

      <button
        onClick={handleConvert}
        disabled={loading || !documentId}
        className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-indigo-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Converting...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><FileImage size={16} /> {isWordFormat ? 'Convert & Download DOCX' : 'Convert & Download ZIP'}</>}
      </button>
    </div>
  );
}

// ─── Compare Tool ──────────────────────────────────────────────

function CompareTool() {
  const [file1, setFile1] = useState(null);
  const [file2, setFile2] = useState(null);
  const [loading, setLoading] = useState(false);
  const [diffs, setDiffs] = useState(null);
  const file1Ref = useRef(null);
  const file2Ref = useRef(null);

  const handleCompare = async () => {
    if (!file1 || !file2) return;
    setLoading(true);
    setDiffs(null);
    try {
      const result = await comparePdfs(file1, file2);
      setDiffs(result.diffs || []);
    } catch (e) {
      console.error(e);
      alert('Compare failed: ' + e.message);
    } finally {
      setLoading(false);
    }
  };

  const stats = diffs ? {
    added: diffs.filter(d => d.type === 'added').length,
    removed: diffs.filter(d => d.type === 'removed').length,
    equal: diffs.filter(d => d.type === 'equal').length,
  } : null;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-orange-400 mb-1">
        <GitCompareArrows size={20} />
        <h3 className="font-semibold text-white text-base">Compare PDFs</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Compare the text content of two PDF files and see the differences.</p>

      <input ref={file1Ref} type="file" accept="application/pdf" onChange={(e) => { setFile1(e.target.files[0]); setDiffs(null); }} className="hidden" />
      <input ref={file2Ref} type="file" accept="application/pdf" onChange={(e) => { setFile2(e.target.files[0]); setDiffs(null); }} className="hidden" />

      <div className="flex flex-col gap-2">
        <button
          onClick={() => file1Ref.current?.click()}
          className="w-full py-2.5 border-2 border-dashed border-slate-600 rounded-lg text-slate-400 hover:border-orange-500 hover:text-orange-400 transition-all flex items-center justify-center gap-2 text-xs"
        >
          <UploadCloud size={16} />
          {file1 ? `📄 ${file1.name}` : 'Choose First PDF (Original)'}
        </button>
        <button
          onClick={() => file2Ref.current?.click()}
          className="w-full py-2.5 border-2 border-dashed border-slate-600 rounded-lg text-slate-400 hover:border-orange-500 hover:text-orange-400 transition-all flex items-center justify-center gap-2 text-xs"
        >
          <UploadCloud size={16} />
          {file2 ? `📄 ${file2.name}` : 'Choose Second PDF (Modified)'}
        </button>
      </div>

      <button
        onClick={handleCompare}
        disabled={loading || !file1 || !file2}
        className="w-full py-2.5 bg-orange-600 hover:bg-orange-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-orange-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Comparing...</> :
         <><GitCompareArrows size={16} /> Compare</>}
      </button>

      {stats && (
        <div className="flex gap-2 text-[10px]">
          <span className="px-2 py-1 bg-green-900/30 text-green-400 rounded">+{stats.added} added</span>
          <span className="px-2 py-1 bg-red-900/30 text-red-400 rounded">−{stats.removed} removed</span>
          <span className="px-2 py-1 bg-slate-700/50 text-slate-400 rounded">{stats.equal} unchanged</span>
        </div>
      )}

      {diffs && (
        <div className="max-h-64 overflow-y-auto bg-slate-900/50 rounded-lg border border-slate-700 p-2">
          {diffs.length === 0 ? (
            <p className="text-slate-500 text-xs text-center py-4">Documents are identical.</p>
          ) : (
            <div className="flex flex-col gap-0.5 font-mono text-[11px]">
              {diffs.map((diff, i) => (
                <div
                  key={i}
                  className={`px-2 py-0.5 rounded-sm leading-relaxed ${
                    diff.type === 'added' ? 'bg-green-900/30 text-green-300 border-l-2 border-green-500' :
                    diff.type === 'removed' ? 'bg-red-900/30 text-red-300 border-l-2 border-red-500' :
                    'text-slate-500'
                  }`}
                >
                  <span className="select-none mr-1 opacity-50">
                    {diff.type === 'added' ? '+' : diff.type === 'removed' ? '−' : ' '}
                  </span>
                  {diff.text || '\u00A0'}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

// ─── Redact Tool ───────────────────────────────────────────────

function RedactTool({ documentId, toolState, setToolState }) {
  const [color, setColor] = useState('#000000');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  React.useEffect(() => {
    if (setToolState) {
        setToolState(prev => ({
            ...prev,
            active: true,
            type: 'redact',
            color: color,
            boxes: prev?.boxes || []
        }));
    }
  }, [color]);

  const handleApply = async () => {
    if (!documentId || !toolState || !toolState.boxes || toolState.boxes.length === 0) return;
    setLoading(true);
    setDone(false);
    try {
      const redactRequestDto = {
        documentId: documentId,
        clientPageWidth: toolState.clientPageWidth || 0,
        clientPageHeight: toolState.clientPageHeight || 0,
        boxes: toolState.boxes
      };
      // Note: Make sure to import redactPdfBoxes from your api service in ToolPanel.jsx if not already
      const { redactPdfBoxes } = await import('../services/toolsApi');
      const blob = await redactPdfBoxes(redactRequestDto);
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'redacted.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Redact failed: ' + (e.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    if (setToolState && toolState) {
        setToolState({ ...toolState, boxes: [] });
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-red-400 mb-1">
        <EyeOff size={20} />
        <h3 className="font-semibold text-white text-base">Redact Area</h3>
      </div>
      
      <div className="bg-red-900/30 border border-red-500/40 rounded-lg p-3 text-center mb-1">
        <div className="w-10 h-10 bg-red-800/50 rounded-full flex items-center justify-center mx-auto mb-2">
           <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-red-200">
             <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
             <line x1="3" y1="9" x2="21" y2="9"></line>
             <line x1="9" y1="21" x2="9" y2="9"></line>
           </svg>
        </div>
        <p className="text-white font-medium text-sm">Add a Redaction Box</p>
        <p className="text-slate-300 text-[11px] mt-1.5 leading-relaxed mb-3">
          Click the button below to add a box. You can then <strong>drag it around</strong> and <strong>resize it</strong> on the PDF document.
        </p>

        <button
          onClick={() => {
            if (setToolState && toolState) {
              setToolState({
                ...toolState,
                boxes: [...(toolState.boxes || []), {
                  id: Date.now().toString(),
                  page: 1, // Usually the user is looking at page 1 or they can scroll.
                  color: color,
                  x: 100, y: 100, width: 250, height: 60
                }]
              });
            }
          }}
          className="w-full py-2 bg-red-600 hover:bg-red-700 text-white rounded font-medium text-sm transition-all shadow-md flex items-center justify-center gap-2"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          Add Box
        </button>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Redaction Color</label>
        <div className="flex items-center gap-2">
          <div className="flex gap-1.5">
            {['#000000', '#FF0000', '#333333', '#FFFFFF'].map(c => (
              <button
                key={c}
                onClick={() => setColor(c)}
                className={`w-7 h-7 rounded-md border-2 transition-all ${
                  color === c 
                    ? 'border-red-400 scale-110 shadow-md shadow-red-600/20' 
                    : 'border-slate-600 hover:border-slate-400'
                }`}
                style={{ backgroundColor: c }}
                title={c}
              />
            ))}
          </div>
          <input
            type="color"
            value={color}
            onChange={(e) => setColor(e.target.value)}
            className="w-8 h-7 rounded border border-slate-600 cursor-pointer bg-transparent"
          />
          <span className="text-xs text-slate-400 font-mono">{color}</span>
        </div>
      </div>

      <div className="flex gap-2 mt-2">
          <button
            onClick={handleClear}
            className="flex-1 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded text-sm font-medium transition-colors"
          >
            Clear Boxes
          </button>
          <button
            onClick={handleApply}
            disabled={loading || !toolState || !toolState.boxes || toolState.boxes.length === 0}
            className="flex-1 py-2 bg-red-600 hover:bg-red-700 disabled:opacity-50 text-white rounded text-sm font-medium transition-colors shadow-md shadow-red-600/20"
          >
            {loading ? 'Processing...' : 'Apply Redaction'}
          </button>
      </div>

      <div className="bg-amber-900/20 border border-amber-700/30 rounded-lg p-2.5 flex items-start gap-2 mt-2">
        <AlertCircle size={14} className="text-amber-400 flex-shrink-0 mt-0.5" />
        <p className="text-amber-300/80 text-[10px] leading-relaxed">
          Redaction is permanent. The original text and imagery under the redaction box will be visually hidden. Save your original file before redacting.
        </p>
      </div>

      {done && <p className="text-red-400 text-xs text-center mt-1 flex items-center justify-center gap-1"><CheckCircle size={14}/> Redacted document ready!</p>}
    </div>
  );
}

// ─── OCR Tool ─────────────────────────────────────────────────

function OcrTool({ documentId }) {
  const [language, setLanguage] = useState('eng');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const languages = [
    { value: 'eng', label: '🇬🇧 English' },
    { value: 'ind', label: '🇮🇩 Indonesian' },
    { value: 'fra', label: '🇫🇷 French' },
    { value: 'deu', label: '🇩🇪 German' },
    { value: 'spa', label: '🇪🇸 Spanish' },
    { value: 'ita', label: '🇮🇹 Italian' },
    { value: 'por', label: '🇵🇹 Portuguese' },
    { value: 'nld', label: '🇳🇱 Dutch' },
    { value: 'jpn', label: '🇯🇵 Japanese' },
    { value: 'kor', label: '🇰🇷 Korean' },
    { value: 'chi_sim', label: '🇨🇳 Chinese (Simplified)' },
    { value: 'ara', label: '🇸🇦 Arabic' },
  ];

  const handleOcr = async () => {
    if (!documentId) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await ocrPdf(documentId, language);
      downloadBlob(blob, 'ocr_output.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('OCR failed: ' + (e.message || 'Unknown error. Make sure Tesseract is installed.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-emerald-400 mb-1">
        <ScanSearch size={20} />
        <h3 className="font-semibold text-white text-base">OCR (Text Recognition)</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Extract text from scanned or image-based PDFs using Tesseract OCR. Outputs a searchable PDF.</p>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">OCR Language</label>
        <select
          value={language}
          onChange={(e) => { setLanguage(e.target.value); setDone(false); }}
          className="w-full bg-slate-700/60 border border-slate-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500/30 appearance-none cursor-pointer"
        >
          {languages.map(lang => (
            <option key={lang.value} value={lang.value}>{lang.label}</option>
          ))}
        </select>
        <p className="text-slate-500 text-[10px] mt-1.5">Select the primary language of the scanned document.</p>
      </div>

      <div className="bg-slate-700/30 border border-slate-600/50 rounded-lg p-2.5">
        <p className="text-slate-400 text-[10px] leading-relaxed">
          ℹ️ <strong className="text-slate-300">How it works:</strong> Each page is rendered at 300 DPI, then processed by Tesseract OCR.
          The output PDF contains the original visuals with an invisible searchable text layer.
        </p>
      </div>

      <div className="bg-amber-900/20 border border-amber-700/30 rounded-lg p-2.5 flex items-start gap-2">
        <AlertCircle size={14} className="text-amber-400 flex-shrink-0 mt-0.5" />
        <p className="text-amber-300/80 text-[10px] leading-relaxed">
          Requires Tesseract installed on the server. Processing may take a while for large documents.
        </p>
      </div>

      <button
        onClick={handleOcr}
        disabled={loading || !documentId}
        className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg flex items-center justify-center gap-2 text-sm font-medium transition-all shadow-md shadow-emerald-600/20"
      >
        {loading ? <><Loader2 size={16} className="animate-spin" /> Processing OCR...</> :
         done ? <><Check size={16} /> Downloaded!</> :
         <><ScanSearch size={16} /> Run OCR & Download</>}
      </button>
    </div>
  );
}

// ─── Main ToolPanel ─────────────────────────────────────────────

function SignTool({ documentId, toolState, setToolState }) {
  const [signatureFile, setSignatureFile] = React.useState(null);
  const [page, setPage] = React.useState(1);
  const [x, setX] = React.useState(50);
  const [y, setY] = React.useState(50);
  const [scale, setScale] = React.useState(0.5);
  const [loading, setLoading] = React.useState(false);
  const [done, setDone] = React.useState(false);
  const fileInputRef = React.useRef(null);

  React.useEffect(() => {
    if (signatureFile && setToolState) {
        setToolState({
            active: true,
            type: 'sign',
            url: URL.createObjectURL(signatureFile),
            page, x, y, scale
        });
    } else if (setToolState && toolState && toolState.type === 'sign') {
        setToolState(null);
    }
  }, [signatureFile, page, x, y, scale]);

  const handleApply = async () => {
    if (!documentId || !signatureFile) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await signPdf(documentId, signatureFile, page, x, y, scale);
      downloadBlob(blob, 'signed.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Sign failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-indigo-400 mb-1">
        <PenTool size={20} />
        <h3 className="font-semibold text-white text-base">Sign Document</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Place a visual signature image onto the document.</p>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/png,image/jpeg"
        onChange={(e) => { setSignatureFile(e.target.files[0]); setDone(false); }}
        className="hidden"
      />

      <button
        onClick={() => fileInputRef.current?.click()}
        className="w-full py-3 border-2 border-dashed border-slate-600 rounded-lg text-slate-400 hover:border-indigo-500 hover:text-indigo-400 transition-all flex items-center justify-center gap-2 text-sm"
      >
        <UploadCloud size={18} />
        {signatureFile ? signatureFile.name : 'Upload Signature (PNG/JPG)'}
      </button>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Page Number</label>
        <input 
          type="number" min="1" value={page} onChange={e => setPage(parseInt(e.target.value) || 1)}
          className="w-full bg-slate-800 border border-slate-600 rounded px-3 py-2 text-sm text-slate-200 outline-none focus:border-indigo-500"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Horizontal Position: {x}%</label>
        <input type="range" value={x} onChange={e => setX(Number(e.target.value))} min="0" max="100" step="1" className="w-full accent-indigo-500" />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Vertical Position: {y}%</label>
        <input type="range" value={y} onChange={e => setY(Number(e.target.value))} min="0" max="100" step="1" className="w-full accent-indigo-500" />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Scale/Size: {scale}x</label>
        <input type="range" value={scale} onChange={e => setScale(Number(e.target.value))} min="0.1" max="2" step="0.05" className="w-full accent-indigo-500" />
      </div>

      <button
        onClick={handleApply}
        disabled={loading || !signatureFile}
        className="w-full mt-2 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded text-sm font-medium transition-colors"
      >
        {loading ? 'Processing...' : 'Apply Signature'}
      </button>

      {done && <p className="text-indigo-400 text-xs text-center mt-2 flex items-center justify-center gap-1"><CheckCircle size={14}/> Signed document ready!</p>}
    </div>
  );
}

// ─── Draw Tool ──────────────────────────────────────────────────

function DrawTool({ documentId, toolState, setToolState }) {
  const [color, setColor] = useState('#fbbf24');
  const [thickness, setThickness] = useState(5);
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  // Initialize toolState for drawing
  React.useEffect(() => {
    if (setToolState) {
        setToolState({
            active: true,
            type: 'draw',
            color: color,
            thickness: thickness,
            strokes: []
        });
    }
  }, [color, thickness]);

  const handleApply = async () => {
    if (!documentId || !toolState || toolState.strokes.length === 0) return;
    setLoading(true);
    setDone(false);
    try {
      const drawRequestDto = {
        documentId: documentId,
        clientPageWidth: toolState.clientPageWidth || 0,
        clientPageHeight: toolState.clientPageHeight || 0,
        strokes: toolState.strokes
      };
      const blob = await drawOnPdf(drawRequestDto);
      downloadBlob(blob, 'drawn.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Draw failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    if (setToolState && toolState) {
        setToolState({ ...toolState, strokes: [] });
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-yellow-400 mb-1">
        <Edit3 size={20} />
        <h3 className="font-semibold text-white text-base">Draw / Highlight</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Draw on the PDF directly. Use thick yellow for highlighting.</p>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Color</label>
        <div className="flex gap-2">
            {['#fbbf24', '#f87171', '#ef4444', '#3b82f6', '#10b981', '#000000'].map(c => (
                <button 
                  key={c} 
                  onClick={() => setColor(c)}
                  className={`w-8 h-8 rounded-full border-2 ${color === c ? 'border-white' : 'border-transparent'}`}
                  style={{ backgroundColor: c }}
                />
            ))}
        </div>
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Thickness: {thickness}px</label>
        <input 
          type="range" value={thickness} onChange={e => setThickness(Number(e.target.value))} 
          min="1" max="25" step="1" 
          className="w-full accent-yellow-500" 
        />
      </div>

      <div className="flex gap-2 mt-2">
          <button
            onClick={handleClear}
            className="flex-1 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded text-sm font-medium transition-colors"
          >
            Clear Strokes
          </button>
          <button
            onClick={handleApply}
            disabled={loading || !toolState || !toolState.strokes || toolState.strokes.length === 0}
            className="flex-1 py-2 bg-yellow-600 hover:bg-yellow-700 disabled:opacity-50 text-white rounded text-sm font-medium transition-colors"
          >
            {loading ? 'Processing...' : 'Apply Drawing'}
          </button>
      </div>

      {done && <p className="text-yellow-400 text-xs text-center mt-2 flex items-center justify-center gap-1"><CheckCircle size={14}/> Drawn document ready!</p>}
    </div>
  );
}

// ─── Insert Image Tool ──────────────────────────────────────────

function InsertImageTool({ documentId, toolState, setToolState }) {
  const [imageFile, setImageFile] = React.useState(null);
  const [page, setPage] = React.useState(1);
  const [x, setX] = React.useState(50);
  const [y, setY] = React.useState(50);
  const [scale, setScale] = React.useState(1.0);
  const [loading, setLoading] = React.useState(false);
  const [done, setDone] = React.useState(false);
  const fileInputRef = React.useRef(null);

  React.useEffect(() => {
    if (imageFile && setToolState) {
        setToolState({
            active: true,
            type: 'insertImage',
            url: URL.createObjectURL(imageFile),
            page, x, y, scale
        });
    } else if (setToolState && toolState && toolState.type === 'insertImage') {
        setToolState(null);
    }
  }, [imageFile, page, x, y, scale]);

  const handleApply = async () => {
    if (!documentId || !imageFile) return;
    setLoading(true);
    setDone(false);
    try {
      const blob = await insertImageOnPdf(documentId, imageFile, page, x, y, scale);
      downloadBlob(blob, 'with-image.pdf');
      setDone(true);
    } catch (e) {
      console.error(e);
      alert('Insert Image failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-pink-400 mb-1">
        <ImagePlus size={20} />
        <h3 className="font-semibold text-white text-base">Insert Image</h3>
      </div>
      <p className="text-slate-400 text-xs leading-relaxed">Add a custom image cleanly anywhere on a page.</p>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/png,image/jpeg"
        onChange={(e) => { setImageFile(e.target.files[0]); setDone(false); }}
        className="hidden"
      />

      <button
        onClick={() => fileInputRef.current?.click()}
        className="w-full py-3 border-2 border-dashed border-slate-600 rounded-lg text-slate-400 hover:border-pink-500 hover:text-pink-400 transition-all flex items-center justify-center gap-2 text-sm"
      >
        <UploadCloud size={18} />
        {imageFile ? imageFile.name : 'Upload Image (PNG/JPG)'}
      </button>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Page Number</label>
        <input 
          type="number" min="1" value={page} onChange={e => setPage(parseInt(e.target.value) || 1)}
          className="w-full bg-slate-800 border border-slate-600 rounded px-3 py-2 text-sm text-slate-200 outline-none focus:border-pink-500"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Horizontal Position: {x}%</label>
        <input type="range" value={x} onChange={e => setX(Number(e.target.value))} min="0" max="100" step="1" className="w-full accent-pink-500" />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Vertical Position: {y}%</label>
        <input type="range" value={y} onChange={e => setY(Number(e.target.value))} min="0" max="100" step="1" className="w-full accent-pink-500" />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-400 mb-1.5">Scale/Size: {scale}x</label>
        <input type="range" value={scale} onChange={e => setScale(Number(e.target.value))} min="0.1" max="5" step="0.1" className="w-full accent-pink-500" />
      </div>

      <button
        onClick={handleApply}
        disabled={loading || !imageFile}
        className="w-full mt-2 py-2 bg-pink-600 hover:bg-pink-700 disabled:opacity-50 text-white rounded text-sm font-medium transition-colors"
      >
        {loading ? 'Processing...' : 'Apply Image'}
      </button>

      {done && <p className="text-pink-400 text-xs text-center mt-2 flex items-center justify-center gap-1"><CheckCircle size={14}/> Image inserted successfully!</p>}
    </div>
  );
}

export default function ToolPanel({ tool, documentId, onClose, toolState, setToolState }) {
  const renderTool = () => {
    switch (tool) {
      case 'draw':     return <DrawTool documentId={documentId} toolState={toolState} setToolState={setToolState} />;
      case 'insertImage': return <InsertImageTool documentId={documentId} toolState={toolState} setToolState={setToolState} />;
      case 'compress': return <CompressTool documentId={documentId} />;
      case 'password': return <PasswordTool documentId={documentId} />;
      case 'merge':    return <MergeTool />;
      case 'watermark': return <WatermarkTool documentId={documentId} />;
      case 'stamp':    return <StampTool documentId={documentId} />;
      case 'split':    return <SplitTool documentId={documentId} />;
      case 'arrange':  return <ArrangeTool documentId={documentId} />;
      case 'convert':  return <ConvertTool documentId={documentId} />;
      case 'compare':  return <CompareTool />;
      case 'redact':   return <RedactTool documentId={documentId} toolState={toolState} setToolState={setToolState} />;
      case 'ocr':      return <OcrTool documentId={documentId} />;
      case 'sign':     return <SignTool documentId={documentId} toolState={toolState} setToolState={setToolState} />;
      default: return null;
    }
  };

  return (
    <div className="w-80 bg-slate-800 border-l border-slate-700 flex flex-col overflow-hidden">
      <div className="flex items-center justify-between px-4 py-3 border-b border-slate-700/50 bg-slate-800/80">
        <span className="text-xs uppercase font-bold text-slate-400 tracking-wider">PDF Tool</span>
        <button 
          onClick={onClose}
          className="p-1 rounded hover:bg-slate-700 text-slate-400 hover:text-white transition-colors"
        >
          <X size={16} />
        </button>
      </div>
      <div className="flex-1 overflow-y-auto p-4">
        {renderTool()}
      </div>
    </div>
  );
}
