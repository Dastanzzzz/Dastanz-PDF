package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PdfArrangeService {

    /**
     * Arrange pages of a PDF: reorder, delete, rotate, and duplicate.
     *
     * The {@code pages} list defines the output document:
     * - Each entry specifies a source page index (0-based) and a rotation (0/90/180/270).
     * - Pages not listed are effectively deleted.
     * - A page listed twice is duplicated.
     * - The output order matches the list order.
     *
     * @param pdfFile  the source PDF file
     * @param pages    list of page instructions (sourcePage + rotation)
     * @return         byte[] of the rearranged PDF
     */
    public byte[] arrange(File pdfFile, List<PageInstruction> pages) throws IOException {
        if (pages == null || pages.isEmpty()) {
            throw new IllegalArgumentException("Page instructions cannot be empty");
        }

        try (PDDocument source = PDDocument.load(pdfFile);
             PDDocument result = new PDDocument()) {

            int totalPages = source.getNumberOfPages();

            for (PageInstruction instruction : pages) {
                int srcIndex = instruction.getSourcePage();
                if (srcIndex < 0 || srcIndex >= totalPages) {
                    throw new IllegalArgumentException(
                            "Source page " + srcIndex + " is out of range (0-" + (totalPages - 1) + ")");
                }

                PDPage originalPage = source.getPages().get(srcIndex);
                PDPage importedPage = result.importPage(originalPage);

                // Apply rotation if specified
                int rotation = normalizeRotation(instruction.getRotation());
                if (rotation != 0) {
                    int currentRotation = importedPage.getRotation();
                    importedPage.setRotation((currentRotation + rotation) % 360);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            result.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Normalize rotation to 0, 90, 180, or 270.
     */
    private int normalizeRotation(int rotation) {
        int r = rotation % 360;
        if (r < 0) r += 360;
        // Snap to nearest valid rotation
        if (r >= 0 && r < 45) return 0;
        if (r >= 45 && r < 135) return 90;
        if (r >= 135 && r < 225) return 180;
        if (r >= 225 && r < 315) return 270;
        return 0;
    }

    /**
     * Represents a single page instruction for the arrange operation.
     */
    public static class PageInstruction {
        private int sourcePage;
        private int rotation;

        public PageInstruction() {}

        public PageInstruction(int sourcePage, int rotation) {
            this.sourcePage = sourcePage;
            this.rotation = rotation;
        }

        public int getSourcePage() { return sourcePage; }
        public void setSourcePage(int sourcePage) { this.sourcePage = sourcePage; }
        public int getRotation() { return rotation; }
        public void setRotation(int rotation) { this.rotation = rotation; }
    }
}
