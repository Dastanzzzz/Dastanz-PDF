package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.model.PageTextBlock;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfTextBlockExtractorService {

    public List<PageTextBlock> extractTextBlocks(InputStream pdfInputStream) throws IOException {
        List<PageTextBlock> blocks = new ArrayList<>();
        
        try (PDDocument document = PDDocument.load(pdfInputStream)) {
            PDFTextStripper stripper = new CustomPDFTextStripper(blocks);
            stripper.setSortByPosition(true);
            
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                stripper.getText(document);
            }
        }
        return blocks;
    }
    
    private static class CustomPDFTextStripper extends PDFTextStripper {
        private final List<PageTextBlock> blocks;
        private int blockIdCounter = 1;
        private int currentPage = 1;

        public CustomPDFTextStripper(List<PageTextBlock> blocks) throws IOException {
            super();
            this.blocks = blocks;
        }

        @Override
        protected void startPage(org.apache.pdfbox.pdmodel.PDPage page) throws IOException {
            super.startPage(page);
            this.currentPage = getCurrentPageNo();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            if (text == null || text.trim().isEmpty() || textPositions.isEmpty()) {
                return;
            }
            
            TextPosition first = textPositions.get(0);
            TextPosition last = textPositions.get(textPositions.size() - 1);
            
            float x = first.getXDirAdj();
            float y = first.getYDirAdj() - first.getHeightDir();
            float width = last.getXDirAdj() + last.getWidthDirAdj() - first.getXDirAdj();
            float height = first.getHeightDir(); // Rough approx
            float fontSize = first.getFontSizeInPt();
            String fontFamily = first.getFont().getName();

            PageTextBlock block = new PageTextBlock(
                blockIdCounter++,
                currentPage,
                text.trim(),
                x,
                y,
                width,
                height,
                fontSize,
                fontFamily
            );
            blocks.add(block);
        }
    }
}
