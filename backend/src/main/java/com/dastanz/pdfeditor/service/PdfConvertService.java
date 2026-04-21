package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfConvertService {

    /**
     * Convert each page of a PDF to an image and bundle them into a ZIP.
     *
     * @param pdfFile  the source PDF file
     * @param format   image format: "png" or "jpeg"
     * @param dpi      resolution in dots per inch (e.g. 150, 300)
     * @return         byte[] of the ZIP archive containing all page images
     */
    public byte[] convertToImages(File pdfFile, String format, int dpi) throws IOException {
        String imageFormat = normalizeFormat(format);
        String extension = "png".equals(imageFormat) ? ".png" : ".jpg";

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipBytes)) {
                for (int i = 0; i < totalPages; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);

                    ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                    ImageIO.write(image, imageFormat, imgBytes);

                    String entryName = String.format("page_%03d%s", i + 1, extension);
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    zos.write(imgBytes.toByteArray());
                    zos.closeEntry();
                }
            }

            return zipBytes.toByteArray();
        }
    }

    /**
     * Convert a PDF document into a DOCX file by extracting text page-by-page.
     * This is a conservative text-focused conversion (not full visual layout fidelity).
     */
    public byte[] convertToWord(File pdfFile) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(pdfFile);
             XWPFDocument wordDocument = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdfDocument.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String pageText = stripper.getText(pdfDocument);
                String normalizedText = pageText == null ? "" : pageText.replace("\r\n", "\n").replace("\r", "\n");
                String[] lines = normalizedText.split("\n", -1);

                if (lines.length == 0) {
                    wordDocument.createParagraph();
                } else {
                    for (String line : lines) {
                        XWPFParagraph paragraph = wordDocument.createParagraph();
                        XWPFRun run = paragraph.createRun();
                        run.setText(line);
                    }
                }

                if (page < totalPages) {
                    XWPFParagraph pageBreakParagraph = wordDocument.createParagraph();
                    XWPFRun pageBreakRun = pageBreakParagraph.createRun();
                    pageBreakRun.addBreak(BreakType.PAGE);
                }
            }

            wordDocument.write(out);
            return out.toByteArray();
        }
    }

    private String normalizeFormat(String format) {
        if (format == null) return "png";
        String lower = format.toLowerCase().trim();
        if ("jpeg".equals(lower) || "jpg".equals(lower)) {
            return "jpeg";
        }
        return "png";
    }
}
