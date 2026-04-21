package com.dastanz.pdfeditor.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PdfFontService {

    /**
     * Scans the document for fonts and returns basic metadata.
     * This is a v2+ groundwork endpoint. Future versions will extract the raw subsets to /tmp.
     */
    public List<FontMetadata> extractFontMetadata(File pdfFile) throws IOException {
        Set<String> uniqueFonts = new HashSet<>();
        List<FontMetadata> discovered = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                PDResources resources = page.getResources();
                if (resources != null) {
                    for (org.apache.pdfbox.cos.COSName fontName : resources.getFontNames()) {
                        PDFont font = resources.getFont(fontName);
                        String name = font.getName();
                        
                        if (!uniqueFonts.contains(name)) {
                            uniqueFonts.add(name);
                            boolean isEmbedded = font.isEmbedded();
                            discovered.add(new FontMetadata(name, font.getType(), isEmbedded));
                        }
                    }
                }
            }
        }
        
        return discovered;
    }

    public static class FontMetadata {
        public String name;
        public String format; // Type1, TrueType, Type0 etc.
        public boolean isEmbedded;

        public FontMetadata(String name, String format, boolean isEmbedded) {
            this.name = name;
            this.format = format;
            this.isEmbedded = isEmbedded;
        }
    }
}