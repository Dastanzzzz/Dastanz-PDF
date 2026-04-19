package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfStampService {

    /**
     * Add an image stamp to pages of a PDF.
     *
     * @param pdfFile       the source PDF file
     * @param stampImageBytes the stamp image bytes (PNG or JPEG)
     * @param position      position preset: "center", "top-left", "top-right", "bottom-left", "bottom-right"
     * @param scale         scale factor (1.0 = original size, 0.5 = half size)
     * @param opacity       opacity from 0.0 to 1.0
     * @param pageSelection which pages: "all", "first", "last"
     * @return              byte[] of the stamped PDF
     */
    public byte[] addStamp(File pdfFile, byte[] stampImageBytes, String position,
                           float scale, float opacity, String pageSelection) throws IOException {
        // Write stamp image to a temp file for PDImageXObject loading
        File tempStamp = File.createTempFile("stamp_", ".png");
        try {
            java.nio.file.Files.write(tempStamp.toPath(), stampImageBytes);

            try (PDDocument document = PDDocument.load(pdfFile)) {
                PDImageXObject stampImage = PDImageXObject.createFromFileByContent(tempStamp, document);
                float stampWidth = stampImage.getWidth() * scale;
                float stampHeight = stampImage.getHeight() * scale;

                int totalPages = document.getNumberOfPages();

                for (int i = 0; i < totalPages; i++) {
                    if (!shouldStampPage(i, totalPages, pageSelection)) continue;

                    PDPage page = document.getPage(i);
                    float pageWidth = page.getCropBox().getWidth();
                    float pageHeight = page.getCropBox().getHeight();

                    float[] pos = calculatePosition(position, pageWidth, pageHeight, stampWidth, stampHeight);

                    try (PDPageContentStream cs = new PDPageContentStream(
                            document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                        // Set transparency
                        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                        gs.setNonStrokingAlphaConstant(opacity);
                        cs.setGraphicsStateParameters(gs);

                        cs.drawImage(stampImage, pos[0], pos[1], stampWidth, stampHeight);
                    }
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                document.save(out);
                return out.toByteArray();
            }
        } finally {
            tempStamp.delete();
        }
    }

    private boolean shouldStampPage(int pageIndex, int totalPages, String pageSelection) {
        if (pageSelection == null || "all".equalsIgnoreCase(pageSelection)) return true;
        if ("first".equalsIgnoreCase(pageSelection)) return pageIndex == 0;
        if ("last".equalsIgnoreCase(pageSelection)) return pageIndex == totalPages - 1;
        return true;
    }

    private float[] calculatePosition(String position, float pageWidth, float pageHeight,
                                       float stampWidth, float stampHeight) {
        float margin = 20f;
        if (position == null) position = "center";

        switch (position.toLowerCase()) {
            case "top-left":
                return new float[]{margin, pageHeight - stampHeight - margin};
            case "top-right":
                return new float[]{pageWidth - stampWidth - margin, pageHeight - stampHeight - margin};
            case "bottom-left":
                return new float[]{margin, margin};
            case "bottom-right":
                return new float[]{pageWidth - stampWidth - margin, margin};
            case "center":
            default:
                return new float[]{(pageWidth - stampWidth) / 2, (pageHeight - stampHeight) / 2};
        }
    }
}
