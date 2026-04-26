!macro customInstall
  DetailPrint "Checking for OCR Installer..."
  IfFileExists "$INSTDIR\resources\ocr\tesseract-setup.exe" 0 +3
    DetailPrint "OCR Installer found. Starting installation..."
    ExecWait '"$INSTDIR\resources\ocr\tesseract-setup.exe"'
    Goto +2
    DetailPrint "OCR Installer NOT found at $INSTDIR\resources\ocr\tesseract-setup.exe"
!macroend
