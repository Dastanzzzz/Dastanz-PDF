package com.dastanz.pdfeditor.dto;

import lombok.Data;
import java.util.List;

@Data
public class DrawRequestDto {
    private String documentId;
    private float clientPageWidth;
    private float clientPageHeight;
    private List<DrawStrokeDto> strokes;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public float getClientPageWidth() { return clientPageWidth; }
    public void setClientPageWidth(float clientPageWidth) { this.clientPageWidth = clientPageWidth; }
    public float getClientPageHeight() { return clientPageHeight; }
    public void setClientPageHeight(float clientPageHeight) { this.clientPageHeight = clientPageHeight; }
    public List<DrawStrokeDto> getStrokes() { return strokes; }
    public void setStrokes(List<DrawStrokeDto> strokes) { this.strokes = strokes; }
}