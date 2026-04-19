package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfSignService {

    public byte[] sign(File pdfFile, byte[] imageBytes, int page, float xPct, float yPct, float scale) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            // PDFBox uses 0-indexed pages
            if (page < 1 || page > document.getNumberOfPages()) {
                throw new IllegalArgumentException("Invalid page number.");
            }
            PDPage pdPage = document.getPage(page - 1);
            
            PDImageXObject image = PDImageXObject.createFromByteArray(document, imageBytes, "signature");
            
            float pdfPageWidth = pdPage.getCropBox().getWidth();
            float pdfPageHeight = pdPage.getCropBox().getHeight();
            
            float finalWidth = image.getWidth() * scale;
            float finalHeight = image.getHeight() * scale;
            
            // Calculate absolute PDF points from frontend percentages (0.0 to 100.0)
            float actualX = pdfPageWidth * (xPct / 100f);
            float actualY = pdfPageHeight * (yPct / 100f);
            
            // Translate Top-Left frontend Y to Bottom-Left PDFBox Y
            float pdfY = pdfPageHeight - actualY - finalHeight;

            try (PDPageContentStream cs = new PDPageContentStream(document, pdPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                cs.drawImage(image, actualX, pdfY, finalWidth, finalHeight);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
