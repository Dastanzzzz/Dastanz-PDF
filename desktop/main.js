const { app, BrowserWindow, protocol, net } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const http = require('http');
const fs = require('fs');

let mainWindow;
let backendProcess;

// Register custom protocol BEFORE app is ready
// This makes 'app://' behave like 'https://' so blob URLs, fetch, etc. all work
protocol.registerSchemesAsPrivileged([
  { 
    scheme: 'app', 
    privileges: { 
      secure: true, 
      standard: true, 
      supportFetchAPI: true, 
      stream: true,
      corsEnabled: true,
      bypassCSP: true
    } 
  }
]);

function getBaseResourcesPath() {
  return app.isPackaged 
    ? process.resourcesPath 
    : path.join(__dirname, 'resources');
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      webSecurity: false, // Required: allow app:// to call http://localhost:8080 API
    },
  });

  // Load frontend via custom 'app://' protocol (NOT file://)
  // This ensures blob URLs, PDF.js workers, and all web features work correctly
  mainWindow.loadURL('app://frontend/index.html');

  mainWindow.on('closed', function () {
    mainWindow = null;
  });
}

function startBackend() {
  const baseResourcesPath = getBaseResourcesPath();
  const jarPath = path.join(baseResourcesPath, 'backend.jar');
  const bundledJrePath = path.join(baseResourcesPath, 'jre', 'bin', 'java.exe');
  const { dialog } = require('electron');
  
  let javaPath = 'java'; 
  if (fs.existsSync(bundledJrePath)) {
    javaPath = bundledJrePath;
  }

  // Set working directory to userData (AppData) so backend can write files safely
  const userDataPath = app.getPath('userData');
  
  backendProcess = spawn(javaPath, ['-jar', jarPath], {
    cwd: userDataPath,
    env: { 
      ...process.env, 
      GEMINI_API_KEY: 'dummy-api-key-to-bypass-startup-check',
      TESSERACT_PATH: path.join(baseResourcesPath, 'ocr')
    }
  });

  backendProcess.on('error', (err) => {
    dialog.showErrorBox('Backend Error', `Failed to start backend: ${err.message}\nPath: ${javaPath}`);
  });

  backendProcess.stderr.on('data', (data) => {
    console.error(`Backend Error: ${data}`);
  });

  checkBackendReady(0);
}

function checkBackendReady(attempts) {
  http.get('http://localhost:8080', (res) => {
    console.log('Backend is ready!');
  }).on('error', (err) => {
    if (attempts < 60) {
      setTimeout(() => checkBackendReady(attempts + 1), 1000);
    } else {
      const { dialog } = require('electron');
      dialog.showErrorBox('Backend Timeout', 'The backend server is taking too long to start. This might be due to a port conflict or missing dependencies.');
    }
  });
}

app.on('ready', () => {
  const baseResourcesPath = getBaseResourcesPath();
  const frontendDir = path.join(baseResourcesPath, 'frontend');

  // Register the 'app://' protocol handler to serve frontend files
  // This replaces file:// and makes everything work like a real web server
  protocol.handle('app', (request) => {
    const url = new URL(request.url);
    // Remove leading slash and 'frontend' prefix from pathname
    let filePath = decodeURIComponent(url.pathname);
    // url.pathname will be like '/index.html' 
    filePath = path.join(frontendDir, filePath);
    
    return net.fetch('file:///' + filePath.replace(/\\/g, '/'));
  });

  createWindow(); // Show window immediately
  startBackend();
});

app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') {
    if (backendProcess) {
      backendProcess.kill();
    }
    app.quit();
  }
});

app.on('activate', function () {
  if (mainWindow === null) {
    createWindow();
  }
});
