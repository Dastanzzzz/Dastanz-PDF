# 📄 AI Dastanz PDF Text Editor

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5.x-646CFF?style=flat-square&logo=vite&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-4.x-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)

A full-stack, comprehensive web application designed to securely extract, edit, reconstruct, and manage PDF documents. Leveraging **Artificial Intelligence (Gemini API)** alongside industry-standard tools like **Apache PDFBox** and **Apache POI**, it brings "word-processor-like" fluid text editing, format conversion, and rich interaction to static PDF files.

## 📑 Table of Contents

- [✨ Key Features](#-key-features)
- [🏗️ Tech Stack](#️-tech-stack)
- [📂 Project Structure](#-project-structure)
- [⚠️ Limitations & Tradeoffs](#️-limitations--tradeoffs)
- [🚀 Setup & Configuration](#-setup--configuration)
- [🔮 Future Improvements (v3+)](#-future-improvements-v3)
- [📄 License](#-license)

---

## ✨ Key Features

### 🤖 AI-Powered Editing
- **Smart Text Manipulation**: Rewrite, summarize, or alter PDF text blocks instantly using Gemini AI while maintaining the document's original narrative context.
- **AI Document Chat**: Interactively chat with your PDF to ask context-aware questions and extract insights.
- **AI Translation**: Seamlessly translate targeted text blocks natively within the document layer.
- **Fluid Editing**: Edit text directly on the canvas seamlessly with constrained boundary management.
- **Coordinate-Based Extraction**: Precision layout detection leveraging Apache PDFBox.
- **Rich Interaction**: Interactive SVG-based wrapper canvas for drawing custom highlighting polygons and resizing boundary boxes across pages.
- **Font Extraction**: Deep PDF metadata integration mapping original TrueType/Type1 fonts to precisely match dynamic rendering styles.

### 🛠️ Comprehensive PDF Utilities
- **Markup & Annotation**: Draw freehand lines or apply transparent text highlights directly over your PDF.
- **Insert Custom Images**: Cleanly place and scale external image assets anywhere on your document.
- **Convert File Formats**: Export or convert PDFs seamlessly (e.g., PDF to JPG ZIPs and structurally rich PDF to Word/DOCX capabilities).
- **Split & Merge**: Divide large PDFs into multiple parts or combine several PDFs into one optimized document.
- **Arrange**: Reorder or delete specific pages visually without losing layout metadata.
- **Compress**: Drastically reduce PDF file sizes for network sharing.
- **OCR (Optical Character Recognition)**: Extract and reconstruct text from purely scanned image documents using Tesseract OCR.
- **Compare**: Analyze and generate highlighted visual difference layers between two PDF versions.

### 🔒 Security & Verification
- **Digital & Visual Signatures**: Securely upload and place visual signatures backed by underlying real-time **X.509 PKI cryptographic certificates** ensuring document integrity.
- **Password Protection**: Encrypt and lock PDFs with military-grade passwords.
- **Redaction**: Permanently obscure (black out) sensitive PI data from documents.
- **Watermarks & Stamps**: Apply transparent text or image watermarks for copyright marking and review statuses.

---

## 🏗️ Tech Stack

### **Backend (Monolith API)**
- **Java 17 & Spring Boot 3**: Core REST API framework and service orchestration.
- **Apache PDFBox**: The workhorse module used for parsing files, extracting coordinate-bound text, manipulating geometry, and rewriting PDFs.
- **Apache POI (`poi-ooxml`)**: Handles complex format extraction from PDF layouts into native `.docx` (Microsoft Word) structures.
- **Tesseract OCR**: Image-to-text fallback for non-digital scanned PDFs.
- **Google Gemini API**: Context-aware generative AI.

### **Frontend (SPA)**
- **React 18 & Vite**: Fast, modernized UI rendering and compilation.
- **TailwindCSS**: Utility-first styling powering the document viewing experience, floating toolbars, and dynamic editors.
- **`react-pdf`**: Heavily customized to align an interactive SVG/HTML hybrid coordinate layer securely over the exact rendered PDF canvas.

---

## 📂 Project Structure

```text
📦 ai-pdf-editor
 ┣ 📂 backend            # Spring Boot REST API
 ┃ ┣ 📂 src/main/java    # Controllers, DTOs, Services, Models (Java)
 ┃ ┣ 📂 src/test         # Unit and Integration tests for services
 ┃ ┗ 📜 pom.xml          # Maven dependencies (PDFBox, POI, Spring)
 ┣ 📂 frontend           # Vite React Application
 ┃ ┣ 📂 src/components   # UI elements (PdfViewer, Sidebar, ToolPanel)
 ┃ ┣ 📂 src/services     # API network request handlers
 ┃ ┗ 📜 package.json     # Node.js dependencies
 ┣ 📜 README.md
 ┗ 📜 LICENSE
```

---

## ⚠️ Limitations & Tradeoffs

- **PDF Reflowing**: Standard PDFs are not naturally "flowable". In this implementation, we extract text based on visual layout bounds. AI edits are applied by blanking out the old text coordinates and placing new text onto the newly exposed white rectangle. Slight shifts in exact text layout wrap bounds may occur.
- **Stateless Backend**: Currently, document operations assume small-to-medium digital PDFs and store temporary instances of the document in the backend `/tmp` directory associated with a unique document session ID.
- **Scanned Documents**: The editor strictly targets *digital-first* PDFs to manipulate textual content seamlessly. Tesseract OCR fallback is provided, but non-digital (image-only) documents will have a degraded AI-rewrite experience compared to native digital text strings.

![AI Dastanz PDF Text Editor Screenshot](https://github.com/user-attachments/assets/55da6ea5-0731-43c8-8994-6fef7c6d3b36)

---

## 🚀 Setup & Configuration

### Prerequisites
1. **Java 17+**: Required for the Spring Boot backend. 
   - Ensure the `JAVA_HOME` environment variable is set and `java` / `javac` commands are recognized in your terminal.
2. **Node.js 18+**: Required for the Vite/React frontend.
3. **Maven**: Required to build the Java backend dependencies. Ensure the `mvn` command can be recognized in your terminal.
4. **Tesseract OCR (Optional but recommended)**: Requires a system-level install matching your OS. The application expects the `tessdata` folder at `C:\Program Files\Tesseract-OCR`. Modify `application.properties` if you install it elsewhere.

### 1. Configure the Gemini API Key & S3 Storage
You must obtain an API Key from Google for the Gemini API. Add the key as an environment variable before running the backend:

```bash
# Windows (CMD)
set GEMINI_API_KEY=your_key_here

# Optional: Cloud Blob S3 Storage Config (if storage.type=s3 in application.properties)
set AWS_ACCESS_KEY=your_s3_access_key
set AWS_SECRET_KEY=your_s3_secret_key
set AWS_REGION=us-east-1
set AWS_BUCKET_NAME=my-pdf-bucket

# Windows (PowerShell)
$env:GEMINI_API_KEY="your_key_here" # Use "dummy_key" if you don't have one

# Linux / Mac
export GEMINI_API_KEY=your_key_here
```

> **Note:** If you do not have a Gemini API key, you can provide a dummy key. The application will still start and allow you to use standard PDF utilities (Sign, OCR, Split, Convert to Word, Compress, etc.), but the AI text features will fail gracefully.
> **Storage Note:** By default, the app runs using `/tmp` memory on local storage. To use AWS S3 Storage, change `storage.type=s3` inside `application.properties`.

### 2. Start the Backend
```bash
cd backend
mvn clean spring-boot:run
```
*(The backend REST API will expose itself on `http://localhost:8080`)*

### 3. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
*(The frontend development server spins up on `http://localhost:5173`)*

---

## 🔮 Future Improvements (v3+)
- **Desktop Application Packaging**: Porting the localhost application into a standalone Electron or Tauri wrapper for a native desktop experience without spinning up backend servers manually.
- **Local Offline AI Models**: Integrating local models (e.g., Llama 3 via Ollama) to replace Gemini for completely offline and private AI document processing.

## 📄 License
This project is operating under the MIT License. See `LICENSE` for more information.
