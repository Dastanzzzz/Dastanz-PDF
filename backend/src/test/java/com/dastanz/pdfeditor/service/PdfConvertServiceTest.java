package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfConvertServiceTest {

    @Test
    void convertToWord_returnsDocxWithExtractedText() throws Exception {
        PdfConvertService service = new PdfConvertService();
        File pdfFile = createSamplePdf("Hello Convert to Word");

        byte[] docxBytes = service.convertToWord(pdfFile);

        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 0);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            String text = doc.getParagraphs().stream()
                    .map(p -> p.getText() == null ? "" : p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);
            assertTrue(text.contains("Hello Convert to Word"));
        }
    }

    @Test
    void convertToImages_png_stillReturnsZipWithPngEntry() throws Exception {
        PdfConvertService service = new PdfConvertService();
        File pdfFile = createSamplePdf("Hello PNG");

        byte[] zipBytes = service.convertToImages(pdfFile, "png", 72);

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        File zipFile = File.createTempFile("pdf-convert-test", ".zip");
        zipFile.deleteOnExit();
        Files.write(zipFile.toPath(), zipBytes);

        try (ZipFile zf = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            assertTrue(entries.hasMoreElements());

            ZipEntry first = entries.nextElement();
            assertFalse(first.isDirectory());
            assertTrue(first.getName().endsWith(".png"));

            int count = 1;
            while (entries.hasMoreElements()) {
                entries.nextElement();
                count++;
            }
            assertEquals(1, count);
        }
    }

    private File createSamplePdf(String text) throws IOException {
        File pdfFile = File.createTempFile("pdf-convert-test", ".pdf");
        pdfFile.deleteOnExit();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            document.save(pdfFile);
        }

        return pdfFile;
    }
}