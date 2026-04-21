package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
     * Convert a PDF document into a DOCX file by extracting text and basic formatting page-by-page.
     * This incorporates Level 1 (Fonts/Styles) & Level 2 (Images) fidelity.
     */
    public byte[] convertToWord(File pdfFile) throws IOException {
        try (PDDocument pdfDocument = PDDocument.load(pdfFile);
             XWPFDocument wordDocument = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            int totalPages = pdfDocument.getNumberOfPages();

            for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
                
                // Level 1: Extract Text + Fonts
                StyledPdfTextStripper stripper = new StyledPdfTextStripper(wordDocument);
                stripper.setStartPage(pageNo);
                stripper.setEndPage(pageNo);
                
                // Execute text extraction. It will automatically write into wordDocument 
                // via the overridden methods.
                stripper.getText(pdfDocument);

                PDPage page = pdfDocument.getPage(pageNo - 1);
                PDResources resources = page.getResources();
                
                // Level 2: Extract Images Appended at end of page (simplified positioning)
                if (resources != null) {
                    for (COSName xObjectName : resources.getXObjectNames()) {
                        PDXObject xObject = resources.getXObject(xObjectName);
                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject imageXObject = (PDImageXObject) xObject;
                            BufferedImage bImage = imageXObject.getImage();

                            XWPFParagraph imgParagraph = wordDocument.createParagraph();
                            imgParagraph.setAlignment(ParagraphAlignment.CENTER);
                            XWPFRun imgRun = imgParagraph.createRun();

                            ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                            ImageIO.write(bImage, "png", imgBaos);
                            imgBaos.flush();

                            int format = Document.PICTURE_TYPE_PNG;
                            float width = bImage.getWidth();
                            float height = bImage.getHeight();

                            // Scale to fit standard Word page boundary if too large
                            float maxWidth = 500f;
                            if (width > maxWidth) {
                                float ratio = maxWidth / width;
                                width = width * ratio;
                                height = height * ratio;
                            }

                            try (ByteArrayInputStream is = new ByteArrayInputStream(imgBaos.toByteArray())) {
                                imgRun.addPicture(is, format, "image_" + xObjectName.getName(), 
                                        Units.toEMU(width), Units.toEMU(height));
                            } catch (Exception e) {
                                // Image format failure, skip gracefully
                            }
                        }
                    }
                }

                // Add Page break logic unless it's the last page
                if (pageNo < totalPages) {
                    XWPFParagraph pageBreakParagraph = wordDocument.createParagraph();
                    XWPFRun pageBreakRun = pageBreakParagraph.createRun();
                    pageBreakRun.addBreak(BreakType.PAGE);
                }
            }

            wordDocument.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Inner class mapping PDFBox text layout parsing to POI Word Runs (Level 1).
     */
    private static class StyledPdfTextStripper extends PDFTextStripper {
        private final XWPFDocument wordDoc;
        private XWPFParagraph currentParagraph;

        public StyledPdfTextStripper(XWPFDocument wordDoc) throws IOException {
            super();
            this.wordDoc = wordDoc;
            this.currentParagraph = wordDoc.createParagraph();
            this.setSortByPosition(true); // Maintain layout consistency
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (text == null || text.trim().isEmpty() || textPositions.isEmpty()) {
                currentParagraph.createRun().setText(" ");
                return;
            }

            XWPFRun run = currentParagraph.createRun();
            run.setText(text);

            TextPosition firstPos = textPositions.get(0);
            
            // Map the Font Size
            float fontSize = firstPos.getFontSizeInPt();
            if (fontSize > 4) {
                run.setFontSize((int) fontSize);
            }

            // Map Generic Styling via Naming Convention
            String fontName = firstPos.getFont().getName().toLowerCase();
            if (fontName.contains("bold")) {
                run.setBold(true);
            }
            if (fontName.contains("italic") || fontName.contains("oblique")) {
                run.setItalic(true);
            }
        }

        @Override
        protected void writeLineSeparator() throws IOException {
            if (currentParagraph.getRuns().isEmpty()) {
                currentParagraph.createRun().setText("");
            }
            currentParagraph = wordDoc.createParagraph();
        }

        @Override
        protected void writeWordSeparator() throws IOException {
            currentParagraph.createRun().setText(getWordSeparator());
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
