package com.dastanz.pdfeditor.service;

import com.dastanz.pdfeditor.dto.DrawRequestDto;
import com.dastanz.pdfeditor.dto.DrawStrokeDto;
import com.dastanz.pdfeditor.dto.PointDto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class PdfDrawService {

    public byte[] draw(File pdfFile, DrawRequestDto request) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            for (DrawStrokeDto stroke : request.getStrokes()) {
                if (stroke.getPage() < 1 || stroke.getPage() > document.getNumberOfPages()) {
                    continue;
                }
                PDPage page = document.getPage(stroke.getPage() - 1);
                float pdfPageWidth = page.getCropBox().getWidth();
                float pdfPageHeight = page.getCropBox().getHeight();

                float scaleX = request.getClientPageWidth() > 0 ? pdfPageWidth / request.getClientPageWidth() : 1f;
                float scaleY = request.getClientPageHeight() > 0 ? pdfPageHeight / request.getClientPageHeight() : 1f;

                try (PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    Color color = hex2Rgb(stroke.getColor() != null ? stroke.getColor() : "#000000");
                    cs.setStrokingColor(color);
                    
                    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                    if (stroke.getColor() != null && (stroke.getColor().equalsIgnoreCase("#facc15") || stroke.getColor().equalsIgnoreCase("#fbbf24")) && stroke.getThickness() > 10) {
                        gs.setStrokingAlphaConstant(0.4f);
                        // Highlight mode sets mix blend roughly by using alpha
                    } else {
                        gs.setStrokingAlphaConstant(1.0f);
                    }
                    cs.setGraphicsStateParameters(gs);
                    
                    // Convert stroke thickness using roughly average scale
                    cs.setLineWidth(stroke.getThickness() * ((scaleX + scaleY) / 2));
                    
                    // Round caps and joins for smoother freehand lines
                    cs.setLineCapStyle(1);
                    cs.setLineJoinStyle(1);

                    if (stroke.getPoints() != null && !stroke.getPoints().isEmpty()) {
                        PointDto start = stroke.getPoints().get(0);
                        float startX = start.getX() * scaleX;
                        float startY = pdfPageHeight - (start.getY() * scaleY);
                        
                        cs.moveTo(startX, startY);

                        for (int i = 1; i < stroke.getPoints().size(); i++) {
                            PointDto p = stroke.getPoints().get(i);
                            float x = p.getX() * scaleX;
                            float y = pdfPageHeight - (p.getY() * scaleY);
                            cs.lineTo(x, y);
                        }
                        cs.stroke();
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    private Color hex2Rgb(String colorStr) {
        if (colorStr == null || colorStr.length() < 7) {
            return Color.BLACK;
        }
        return new Color(
            Integer.valueOf(colorStr.substring(1, 3), 16),
            Integer.valueOf(colorStr.substring(3, 5), 16),
            Integer.valueOf(colorStr.substring(5, 7), 16)
        );
    }
}