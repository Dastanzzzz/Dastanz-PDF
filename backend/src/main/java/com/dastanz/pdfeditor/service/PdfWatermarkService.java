package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfWatermarkService {

    /**
     * Add a text watermark to every page of a PDF.
     *
     * @param pdfFile   the source PDF file
     * @param text      watermark text
     * @param fontSize  font size for the watermark
     * @param opacity   opacity from 0.0 (invisible) to 1.0 (fully opaque)
     * @param rotation  rotation angle in degrees (e.g. 45 for diagonal)
     * @param color     hex color string (e.g. "#FF0000"), defaults to gray
     * @return          byte[] of the watermarked PDF
     */
    public byte[] addWatermark(File pdfFile, String text, float fontSize, float opacity,
                                float rotation, String color) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            float[] rgb = parseColor(color);

            for (PDPage page : document.getPages()) {
                float pageWidth = page.getCropBox().getWidth();
                float pageHeight = page.getCropBox().getHeight();

                try (PDPageContentStream cs = new PDPageContentStream(
                        document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                    // Set transparency
                    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                    gs.setNonStrokingAlphaConstant(opacity);
                    gs.setStrokingAlphaConstant(opacity);
                    cs.setGraphicsStateParameters(gs);

                    cs.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);

                    // Position at center of page with rotation
                    float centerX = pageWidth / 2;
                    float centerY = pageHeight / 2;

                    // Approximate text width for centering
                    float textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000 * fontSize;
                    float rad = (float) Math.toRadians(rotation);

                    Matrix matrix = new Matrix();
                    matrix.translate(centerX, centerY);
                    matrix.rotate(rad);
                    matrix.translate(-textWidth / 2, 0);

                    cs.setTextMatrix(matrix);
                    cs.showText(text);
                    cs.endText();
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private float[] parseColor(String hex) {
        if (hex == null || hex.isEmpty() || hex.length() < 6) {
            return new float[]{0.75f, 0.75f, 0.75f}; // default gray
        }
        hex = hex.replace("#", "");
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new float[]{r / 255f, g / 255f, b / 255f};
        } catch (Exception e) {
            return new float[]{0.75f, 0.75f, 0.75f};
        }
    }
}
