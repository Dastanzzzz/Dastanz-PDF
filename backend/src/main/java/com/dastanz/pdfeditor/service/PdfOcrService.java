package com.dastanz.pdfeditor.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfOcrService {

    @Value("${tesseract.datapath:tessdata}")
    private String tessDataPath;

    @Value("${tesseract.language:eng}")
    private String defaultLanguage;

    /**
     * Perform OCR on a scanned/image-based PDF:
     * 1. Render each page as a high-res image
     * 2. Run Tesseract OCR to extract text
     * 3. Build a new PDF with the original image + invisible text overlay
     *
     * @param pdfFile  the source PDF file (scanned/image-based)
     * @param language Tesseract language code (e.g. "eng", "ind", "eng+ind")
     * @return byte[] of the new searchable PDF
     */
    public byte[] ocr(File pdfFile, String language) throws IOException {
        String lang = (language != null && !language.trim().isEmpty()) ? language.trim() : defaultLanguage;

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage(lang);
        // Page segmentation mode 3 = fully automatic page segmentation (default)
        tesseract.setPageSegMode(3);

        try (PDDocument sourceDoc = PDDocument.load(pdfFile);
             PDDocument outputDoc = new PDDocument()) {

            PDFRenderer renderer = new PDFRenderer(sourceDoc);
            int totalPages = sourceDoc.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                // Render page at 300 DPI for good OCR accuracy
                BufferedImage pageImage = renderer.renderImageWithDPI(i, 300, ImageType.RGB);

                // Run OCR on the image
                String ocrText;
                try {
                    ocrText = tesseract.doOCR(pageImage);
                } catch (TesseractException e) {
                    System.err.println("OCR failed on page " + (i + 1) + ": " + e.getMessage());
                    ocrText = ""; // Continue with empty text for this page
                }

                // Create output page with the same dimensions as source
                PDPage sourcePage = sourceDoc.getPage(i);
                PDRectangle mediaBox = sourcePage.getMediaBox();
                PDPage outputPage = new PDPage(mediaBox);
                outputDoc.addPage(outputPage);

                // Draw the original image as the page background
                PDImageXObject pdImage = LosslessFactory.createFromImage(outputDoc, pageImage);
                try (PDPageContentStream cs = new PDPageContentStream(outputDoc, outputPage)) {
                    cs.drawImage(pdImage, 0, 0, mediaBox.getWidth(), mediaBox.getHeight());

                    // Add invisible text overlay for searchability
                    if (ocrText != null && !ocrText.trim().isEmpty()) {
                        cs.beginText();
                        cs.setFont(PDType1Font.HELVETICA, 1); // Tiny invisible font
                        cs.setNonStrokingColor(1f, 1f, 1f); // White (invisible on white bg)
                        // Use rendering mode 3 = invisible
                        cs.setRenderingMode(RenderingMode.NEITHER);
                        cs.newLineAtOffset(0, 0);

                        // Write OCR text line by line
                        String[] lines = ocrText.split("\\r?\\n");
                        for (String line : lines) {
                            if (!line.trim().isEmpty()) {
                                // Sanitize: PDType1Font.HELVETICA only supports WinAnsi
                                String sanitized = sanitizeForWinAnsi(line.trim());
                                if (!sanitized.isEmpty()) {
                                    cs.showText(sanitized);
                                    cs.newLineAtOffset(0, -1.2f);
                                }
                            }
                        }
                        cs.endText();
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            outputDoc.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Strip characters not encodable in WinAnsiEncoding to avoid
     * IllegalArgumentException from PDType1Font.
     */
    private String sanitizeForWinAnsi(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 0x20 && c <= 0x7E) {
                sb.append(c); // Basic ASCII printable
            } else if (c >= 0xA0 && c <= 0xFF) {
                sb.append(c); // Latin-1 supplement
            } else {
                sb.append(' '); // Replace unsupported chars with space
            }
        }
        return sb.toString();
    }
}
