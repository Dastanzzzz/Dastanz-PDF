package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfRedactService {

    /**
     * Redact all occurrences of the given search text in the PDF by drawing
     * filled rectangles over the matching regions.
     *
     * @param pdfFile    the source PDF file
     * @param searchText the text to redact (case-insensitive)
     * @param hexColor   the redaction color in hex format (e.g. "#000000")
     * @return           byte[] of the redacted PDF
     */
    public byte[] redact(File pdfFile, String searchText, String hexColor) throws IOException {
        if (searchText == null || searchText.trim().isEmpty()) {
            throw new IllegalArgumentException("Search text must not be empty");
        }

        Color redactColor = parseColor(hexColor);

        try (PDDocument document = PDDocument.load(pdfFile)) {
            int totalPages = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                List<RedactRegion> regions = findTextRegions(document, pageIndex, searchText);

                if (!regions.isEmpty()) {
                    PDPage page = document.getPage(pageIndex);
                    PDRectangle mediaBox = page.getMediaBox();
                    float pageHeight = mediaBox.getHeight();

                    try (PDPageContentStream cs = new PDPageContentStream(
                            document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                        cs.setNonStrokingColor(redactColor);

                        for (RedactRegion region : regions) {
                            // PDFBox text positions use top-left origin; PDF coordinates use bottom-left.
                            // Convert Y from top-down to bottom-up.
                            float pdfY = pageHeight - region.y - region.height;
                            cs.addRect(region.x, pdfY, region.width, region.height);
                            cs.fill();
                        }
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Find all regions on a given page that contain the search text.
     * Uses a custom PDFTextStripper that captures character-level positions.
     */
    private List<RedactRegion> findTextRegions(PDDocument document, int pageIndex, String searchText)
            throws IOException {
        List<RedactRegion> regions = new ArrayList<>();
        List<TextPosition> allPositions = new ArrayList<>();

        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                allPositions.addAll(textPositions);
                super.writeString(text, textPositions);
            }
        };

        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        stripper.setSortByPosition(true);
        stripper.getText(document);

        // Build a string from all positions and search for occurrences
        StringBuilder fullText = new StringBuilder();
        for (TextPosition tp : allPositions) {
            fullText.append(tp.getUnicode());
        }

        String pageText = fullText.toString();
        String searchLower = searchText.toLowerCase();
        String pageLower = pageText.toLowerCase();

        int searchFrom = 0;
        while (searchFrom < pageLower.length()) {
            int matchIndex = pageLower.indexOf(searchLower, searchFrom);
            if (matchIndex < 0) break;

            int endIndex = Math.min(matchIndex + searchText.length(), allPositions.size());
            if (matchIndex < allPositions.size() && endIndex <= allPositions.size()) {
                // Calculate bounding box for this match
                TextPosition firstChar = allPositions.get(matchIndex);
                TextPosition lastChar = allPositions.get(endIndex - 1);

                float x = firstChar.getXDirAdj();
                float y = firstChar.getYDirAdj() - firstChar.getHeightDir();
                float width = (lastChar.getXDirAdj() + lastChar.getWidthDirAdj()) - x;
                float height = firstChar.getHeightDir();

                // Add small padding for complete coverage
                float padding = 1.0f;
                regions.add(new RedactRegion(
                        x - padding,
                        y - padding,
                        width + (padding * 2),
                        height + (padding * 2)
                ));
            }

            searchFrom = matchIndex + searchText.length();
        }

        return regions;
    }

    private Color parseColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return Color.BLACK;
        }
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    /**
     * Simple record for a rectangular region to be redacted.
     */
    private static class RedactRegion {
        final float x, y, width, height;

        RedactRegion(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
