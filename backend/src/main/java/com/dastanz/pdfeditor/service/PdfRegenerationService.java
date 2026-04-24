package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.model.EditOperation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfRegenerationService {

    public byte[] applyEdits(InputStream pdfInputStream, List<EditOperation> edits, java.util.Map<String, Object> toolState) throws IOException {
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            // Apply Text Edits
            if (edits != null) {
                for (EditOperation edit : edits) {
                    if (edit.getPageNumber() - 1 < 0 || edit.getPageNumber() - 1 >= document.getNumberOfPages())
                        continue;

                    PDPage page = document.getPage(edit.getPageNumber() - 1);

                    try (PDPageContentStream contentStream = new PDPageContentStream(
                            document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                        float cropBoxHeight = page.getCropBox().getHeight();
                        float adjustedY = cropBoxHeight - edit.getY() - edit.getHeight();

                        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f); // white
                        contentStream.addRect(edit.getX() - 2, adjustedY - 2, edit.getWidth() + 4, edit.getHeight() + 4);
                        contentStream.fill();

                        contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f); // black
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, edit.getFontSize());
                        contentStream.newLineAtOffset(edit.getX(), adjustedY + 2);
                        contentStream.showText(edit.getNewText());
                        contentStream.endText();
                    }
                }
            }

            // Apply Tools (Redaction)
            if (toolState != null && toolState.containsKey("boxes")) {
                Object boxesObj = toolState.get("boxes");
                if (boxesObj instanceof java.util.List) {
                    java.util.List<?> boxes = (java.util.List<?>) boxesObj;
                    for (Object b : boxes) {
                        if (b instanceof java.util.Map) {
                            java.util.Map<?, ?> box = (java.util.Map<?, ?>) b;
                            int pageNum = box.get("page") instanceof Number ? ((Number)box.get("page")).intValue() : 1;
                            
                            if (pageNum - 1 < 0 || pageNum - 1 >= document.getNumberOfPages()) continue;
                            
                            PDPage page = document.getPage(pageNum - 1);
                            try (PDPageContentStream contentStream = new PDPageContentStream(
                                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                                
                                float x = box.get("x") instanceof Number ? ((Number)box.get("x")).floatValue() : 0;
                                float y = box.get("y") instanceof Number ? ((Number)box.get("y")).floatValue() : 0;
                                float w = box.get("width") instanceof Number ? ((Number)box.get("width")).floatValue() : 0;
                                float h = box.get("height") instanceof Number ? ((Number)box.get("height")).floatValue() : 0;
                                
                                float cropBoxHeight = page.getCropBox().getHeight();
                                // Assuming React-PDF renders Y from top left
                                float adjustedY = cropBoxHeight - y - h;
                                
                                // Redaction is pitch black fill
                                contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f);
                                contentStream.addRect(x, adjustedY, w, h);
                                contentStream.fill();
                            }
                        }
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
