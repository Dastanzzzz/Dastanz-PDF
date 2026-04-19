# AI Dastanz PDF Text Editor

A full-stack, Java-first web application designed to securely extract, edit, reconstruct, and manage PDF documents using Artificial Intelligence (Gemini API) and comprehensive PDF utilities.

## Key Features

### 🤖 AI-Powered Editing
- **Smart Text Manipulation**: Rewrite, summarize, or alter PDF text blocks instantly using Gemini AI.
- **Fluid Editing**: Edit text seamlessly with maintaining context constraints.
- **Coordinate-Based Extraction**: Precision text selection leveraging Apache PDFBox.

### 🛠️ Comprehensive PDF Utilities
- **Split & Merge**: Divide large PDFs into multiple files or combine several PDFs into one.
- **Arrange**: Reorder or delete specific pages within a PDF.
- **Compress**: Reduce PDF file sizes for much easier sharing.
- **OCR (Optical Character Recognition)**: Extract text from scanned documents using Tesseract OCR.
- **Convert**: Export or convert PDF formats seamlessly.
- **Compare**: Analyze and highlight differences between two PDF documents.

### 🔒 Security & Verification
- **Visual Signatures (v1)**: Securely upload and place visual signatures explicitly on selected pages using real-time coordinates.
- **Password Protection**: Encrypt and lock PDFs with passwords.
- **Redaction**: Permanently obscure sensitive information from documents.
- **Watermarks & Stamps**: Apply text/image watermarks or stamps for copyright and document status.

## Architecture

This project consists of two monolithic services orchestrated through a monorepo structure:
- **Backend (Spring Boot 3 / Java 17+)**: 
  - Orchestrates all PDF handling via `Apache PDFBox`.
  - Exposes clean RESTful APIs for uploads, manipulation, and compilation (`PdfToolsController`, `PdfUploadController`, etc.).
  - Interacts directly with the Gemini REST API to ensure AI calls remain completely server-side.
  - Serves as the immutable single source of truth for saving operations.
- **Frontend (React 18 / Vite)**: 
  - Built using Vite, TailwindCSS v4, and `react-pdf`. 
  - Provides a modern document viewing experience with floating toolbar overlays, right-panel AI editors, and real-time visualization of signatures and edits.
  - Never exposes API keys or sensitive backend credentials.

## Limitations & v1 Tradeoffs
- **PDF Reflowing**: Standard PDFs are not naturally "flowable". In this v1 implementation, we extract text based on visual layout bounds and apply AI edits by blanking out the old text coordinates and placing new text using standard system fonts (Helvetica) onto the newly exposed white rectangle. Shifts in precise text layout may occur.
- **Stateless Backend**: Currently, document operations assume small-to-medium digital PDFs and store temporary instances of the document in the backend `/tmp` directory associated with a unique document ID.
- **Scanned Documents**: v1 targets *digital-first* PDFs to read textual content seamlessly. A Tesseract OCR fallback is provided, but non-digital documents might have degraded AI-rewrite experiences compared to native digital text.

<img width="1910" height="924" alt="{2267A03F-0BD7-4639-A28F-F3ECC4C3B959}" src="https://github.com/user-attachments/assets/55da6ea5-0731-43c8-8994-6fef7c6d3b36" />

## Setup & Configuration

### Prerequisites
- **Java 17+**: Required for the backend. 
  - Download and install the Java Development Kit (JDK) 17 or higher from [Adoptium (Eclipse Temurin)](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/).
  - Ensure the `JAVA_HOME` environment variable is set and `java` / `javac` commands are recognized in your terminal.
- **Node.js 18+**: Required for the frontend.
  - Download and install the LTS version from the [official Node.js website](https://nodejs.org/).
  - This automatically installs `npm` (Node Package Manager). Ensure `node` and `npm` commands are recognized in your CMD or PowerShell.
- **Maven**: Required to build the Java backend. 
  1. Download `apache-maven-3.9.15-bin.zip` from [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
  2. Extract the zip file and move the contents (e.g., the `apache-maven-3.9.15` folder) into your `C:\Program Files\` directory.
  3. Add the Maven `bin` directory (e.g., `C:\Program Files\apache-maven-3.9.15\bin`) to your system's Environment Variables (`Path`) so the `mvn` command can be recognized in CMD or PowerShell.
- **Tesseract OCR**: Required for OCR features to work properly. You can download the Windows installer from [here](https://github.com/UB-Mannheim/tesseract/wiki). The application expects Tesseract to be installed at `C:\Program Files\Tesseract-OCR` (specifically for the `tessdata` folder). If installed elsewhere, you will need to update the `tesseract.datapath` in `application.properties`.

### 1. Configure the Gemini API Key
You must obtain an API Key from Google for the Gemini API. Add the key as an environment variable before running the backend:

```bash
# Windows
set GEMINI_API_KEY=your_key_here

# Linux / Mac
export GEMINI_API_KEY=your_key_here
```

**Note:** If you do not have a Gemini API key, you can use a dummy key (e.g., `set GEMINI_API_KEY="dummy_key_for_testing"`). The application will still run and allow you to use standard PDF tools (like Sign, OCR, Split, Merge, Compress, etc.), but the AI-powered text rewriting features will fail gracefully.

### 2. Start the Backend
```bash
cd backend
mvn clean spring-boot:run
```
*(The backend API runs on `http://localhost:8080`)*

### 3. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
*(The frontend development server runs on `http://localhost:5173` or `5174`)*

## Future Improvements (v2+)
- **Font Extraction**: Expanding Apache PDFBox integration to extract and embed subsets of original TTF fonts back into the file.
- **Rich Interaction**: Drawing custom highlighting polygons and allowing users to resize the newly edited block zones.
- **Digital Cryptographic Signatures**: Upgrading the visual signature V1 feature to include actual PKI-backed certificate signing capabilities.
- **Database Integration**: Swapping the temporary file storage system for a durable blob storage (e.g., S3 or Azure Blob) and a relational database for user sessions.

## License
MIT License. See `LICENSE` for more information.
