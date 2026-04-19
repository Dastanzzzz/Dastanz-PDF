package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfCompressService {

    /**
     * Compress a PDF by re-encoding all embedded images at a lower JPEG quality.
     *
     * @param pdfFile    the source PDF file
     * @param quality    compression quality: "low" (aggressive), "medium", "high" (light)
     * @return           byte[] of the compressed PDF
     */
    public byte[] compress(File pdfFile, String quality) throws IOException {
        float jpegQuality = mapQuality(quality);

        try (PDDocument document = PDDocument.load(pdfFile)) {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject = resources.getXObject(name);
                    if (xobject instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject) xobject;
                        // Re-encode the image as JPEG at the target quality
                        BufferedImage buffered = image.getImage();
                        if (buffered != null) {
                            PDImageXObject compressed = JPEGFactory.createFromImage(document, buffered, jpegQuality);
                            resources.put(name, compressed);
                        }
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private float mapQuality(String quality) {
        if (quality == null) return 0.5f;
        switch (quality.toLowerCase()) {
            case "low":    return 0.3f;   // Aggressive compression
            case "high":   return 0.75f;  // Light compression
            case "medium":
            default:       return 0.5f;   // Balanced
        }
    }
}
