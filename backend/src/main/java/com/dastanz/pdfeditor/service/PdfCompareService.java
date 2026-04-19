package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfCompareService {

    /**
     * Compare two PDFs by extracting their text and performing a line-by-line diff.
     *
     * @param pdf1  input stream of the first PDF
     * @param pdf2  input stream of the second PDF
     * @return      list of diff entries showing equal, added, and removed lines
     */
    public List<DiffEntry> compare(InputStream pdf1, InputStream pdf2) throws IOException {
        String text1 = extractText(pdf1);
        String text2 = extractText(pdf2);

        String[] lines1 = text1.split("\\r?\\n", -1);
        String[] lines2 = text2.split("\\r?\\n", -1);

        return computeDiff(lines1, lines2);
    }

    private String extractText(InputStream pdfStream) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    /**
     * Simple LCS-based line diff algorithm.
     * Returns a list of DiffEntry objects representing the differences.
     */
    private List<DiffEntry> computeDiff(String[] a, String[] b) {
        int m = a.length;
        int n = b.length;

        // Build LCS table
        int[][] dp = new int[m + 1][n + 1];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (a[i].equals(b[j])) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        // Trace back to produce diff
        List<DiffEntry> diffs = new ArrayList<>();
        int i = 0, j = 0;

        while (i < m || j < n) {
            if (i < m && j < n && a[i].equals(b[j])) {
                diffs.add(new DiffEntry("equal", a[i]));
                i++;
                j++;
            } else if (j < n && (i >= m || dp[i][j + 1] >= dp[i + 1][j])) {
                diffs.add(new DiffEntry("added", b[j]));
                j++;
            } else if (i < m) {
                diffs.add(new DiffEntry("removed", a[i]));
                i++;
            }
        }

        return diffs;
    }

    /**
     * Represents a single line in the diff output.
     */
    public static class DiffEntry {
        private String type;  // "equal", "added", "removed"
        private String text;

        public DiffEntry() {}

        public DiffEntry(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
