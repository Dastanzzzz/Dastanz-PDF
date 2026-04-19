package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfSplitService {

    /**
     * Split a PDF by extracting specific pages defined by a range string.
     *
     * @param pdfFile  the source PDF file
     * @param pages    page range string, e.g. "1-3,5,7-9" (1-indexed)
     * @return         byte[] of the new PDF containing only the specified pages
     */
    public byte[] split(File pdfFile, String pages) throws IOException {
        List<Integer> pageNumbers = parsePageRange(pages);

        try (PDDocument source = PDDocument.load(pdfFile);
             PDDocument result = new PDDocument()) {

            int totalPages = source.getNumberOfPages();

            for (int pageNum : pageNumbers) {
                if (pageNum < 1 || pageNum > totalPages) {
                    throw new IllegalArgumentException(
                            "Page " + pageNum + " is out of range (1-" + totalPages + ")");
                }
                PDPage imported = source.getPages().get(pageNum - 1);
                result.importPage(imported);
            }

            if (result.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("No valid pages selected");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            result.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Parse a page range string like "1-3,5,7-9" into a list of page numbers.
     */
    private List<Integer> parsePageRange(String rangeStr) {
        List<Integer> pages = new ArrayList<>();
        if (rangeStr == null || rangeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Page range cannot be empty");
        }

        String[] parts = rangeStr.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] bounds = part.split("-");
                if (bounds.length != 2) {
                    throw new IllegalArgumentException("Invalid range: " + part);
                }
                int start = Integer.parseInt(bounds[0].trim());
                int end = Integer.parseInt(bounds[1].trim());
                if (start > end) {
                    throw new IllegalArgumentException("Invalid range: " + part);
                }
                for (int i = start; i <= end; i++) {
                    pages.add(i);
                }
            } else {
                pages.add(Integer.parseInt(part));
            }
        }

        return pages;
    }
}
