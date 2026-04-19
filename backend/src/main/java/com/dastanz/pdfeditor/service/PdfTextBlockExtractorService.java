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

            // Try to merge with the previous block to form a paragraph
            if (!blocks.isEmpty()) {
                PageTextBlock lastBlock = blocks.get(blocks.size() - 1);
                
                float lastBlockBottom = lastBlock.getY() + lastBlock.getHeight();
                float lastBlockRight = lastBlock.getX() + lastBlock.getWidth();
                
                boolean samePage = lastBlock.getPageNumber() == currentPage;
                // Kita tidak lagi mewajibkan fontFamily sama persis, agar teks tebal/miring (Bold/Italic) tetap menyatu ke paragraf.
                boolean similarFont = Math.abs(lastBlock.getFontSize() - fontSize) < 3.0f;

                // Memastikan teks tersebut ada di baris yang sama secara vertikal, atau baris langsung di bawahnya (paragraf yang sama)
                boolean closeVertically = y >= (lastBlock.getY() - height * 1.0f) && y <= (lastBlockBottom + height * 1.5f);
                
                // Memastikan teks tersebut mengikuti indentasi dari tulisan sebelumnya (baris baru), atau sekadar sambungan teks.
                boolean leftAligned = Math.abs(lastBlock.getX() - x) < (fontSize * 15.0f);
                boolean inlineContinuation = x <= (lastBlockRight + fontSize * 5.0f);
                
                boolean closeHorizontally = leftAligned || inlineContinuation;

                if (samePage && similarFont && closeVertically && closeHorizontally) {
                    // Update bounding box encompassing both lines
                    float newX = Math.min(lastBlock.getX(), x);
                    float newY = Math.min(lastBlock.getY(), y);
                    float newMaxX = Math.max(lastBlock.getX() + lastBlock.getWidth(), x + width);
                    float newMaxY = Math.max(lastBlockBottom, y + height);
                    
                    // Merge text and dimensions
                    lastBlock.setText(lastBlock.getText() + " " + text.trim());
                    lastBlock.setX(newX);
                    lastBlock.setY(newY);
                    lastBlock.setWidth(newMaxX - newX);
                    lastBlock.setHeight(newMaxY - newY);
                    return; // Merged successfully, stop processing
                }
            }

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
