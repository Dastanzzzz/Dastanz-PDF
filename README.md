# AI Dastanz PDF Text Editor

A full-stack, Java-first web application designed to securely extract, edit, and reconstruct PDF documents using Artificial Intelligence (Gemini API).

## Key Features
- **AI-Powered Editing**: Rewrite, summarize, or alter PDF text blocks instantly using Gemini AI.
- **Coordinate-Based Extraction**: Precision text selection leveraging Apache PDFBox.
- **Interactive UI**: A modern React-based interface to preview and apply edits in real-time.
- **Secure by Design**: All AI API calls happen server-side; your API keys are never exposed to the frontend.

## Architecture

This project consists of two monolithic services orchestrated through a monorepo structure:
- **Backend**: Java Spring Boot orchestrates the PDF handling. 
  - Uses `pdfbox` for coordinate-based PDF text block extraction and physical regeneration. 
  - Interacts directly with the Gemini REST API to ensure AI calls remain server-side.
  - Serves as the immutable single source of truth for saving operations.
- **Frontend**: React via Vite + TailwindCSS v4 + `react-pdf`. 
  - Provides a modern document viewing experience. 
  - Interactive right panel handles preview and application of rewritten text.

## Limitations & v1 Tradeoffs
- **PDF Reflowing**: Standard PDFs are not naturally "flowable". In this v1 implementation, we extract text based on visual layout bounds and apply AI edits by blanking out the old text coordinates and placing new text using standard system fonts (Helvetica) onto the newly exposed white rectangle. Shifts in precise text layout may occur.
- **Stateless Backend**: Currently, document operations assume small-to-medium digital PDFs and store temporary instances of the document in the backend `/tmp` directory associated with a unique document ID.
- **Scanned Documents**: v1 targets *digital-first* PDFs to read textual content seamlessly without OCR. A fallback path showing warnings during PDF uploads for images/scanned-only documents is recommended but not included out-of-the-box.

## Setup & Configuration

### Prerequisites
- Java 17+
- Node.js 18+
- Maven

### 1. Configure the Gemini API Key
You must obtain an API Key from Google for the Gemini API. Add the key as an environment variable before running the backend:

```bash
# Windows
set GEMINI_API_KEY=your_key_here

# Linux / Mac
export GEMINI_API_KEY=your_key_here
```

### 2. Start the Backend
```bash
cd backend
mvn clean spring-boot:run
```
*(The backend runs on `http://localhost:8080`)*

### 3. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
*(The frontend runs on `http://localhost:5173`)*

## Future Improvements (v2+)
- **OCR Support**: Integrating Tesseract to seamlessly parse flattened or scanned PDFs.
- **Font Extraction**: Expanding Apache PDFBox integration to extract and embed subsets of original TTF fonts back into the file.
- **Rich Interaction**: Drawing custom highlighting polygons and allowing users to resize the newly edited block zones.

## License
MIT License. See `LICENSE` for more information.
