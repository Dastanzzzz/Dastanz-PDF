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

    public byte[] applyEdits(InputStream pdfInputStream, List<EditOperation> edits) throws IOException {
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            for (EditOperation edit : edits) {
                if (edit.getPageNumber() - 1 < 0 || edit.getPageNumber() - 1 >= document.getNumberOfPages())
                    continue;

                PDPage page = document.getPage(edit.getPageNumber() - 1);

                try (PDPageContentStream contentStream = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                    // 1. Draw a white rectangle to cover the old text
                    contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f); // white
                    // Y axis in PDFBox starts at bottom. If our extraction gave Y from bottom, we
                    // use it directly.
                    // Wait, PDFTextStripper gets Y from top, but PDFBox drawing uses Y from bottom.
                    // We must convert Y if the extraction gave it from top.
                    // Let's assume standard PDF coordinates (origin at bottom left)
                    float cropBoxHeight = page.getCropBox().getHeight();
                    float adjustedY = cropBoxHeight - edit.getY() - edit.getHeight();
                    // Note: This coordinate math is a rough approximation and depends on extraction
                    // specifics

                    // Add some padding to cover text
                    contentStream.addRect(edit.getX() - 2, adjustedY - 2, edit.getWidth() + 4, edit.getHeight() + 4);
                    contentStream.fill();

                    // 2. Draw the new text
                    contentStream.setNonStrokingColor(0.0f, 0.0f, 0.0f); // black
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, edit.getFontSize());
                    contentStream.newLineAtOffset(edit.getX(), adjustedY + 2); // roughly baseline

                    // Note: newText might need line wrapping in a real app, but for v1 we write it
                    // out directly
                    contentStream.showText(edit.getNewText());
                    contentStream.endText();
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
