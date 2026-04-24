package com.dastanz.pdfeditor.dto;

import lombok.Data;
import java.util.List;

@Data
public class RedactBoxRequestDto {
    private String documentId;
    private float clientPageWidth;
    private float clientPageHeight;
    private List<RedactBoxDto> boxes;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public float getClientPageWidth() { return clientPageWidth; }
    public void setClientPageWidth(float clientPageWidth) { this.clientPageWidth = clientPageWidth; }
    public float getClientPageHeight() { return clientPageHeight; }
    public void setClientPageHeight(float clientPageHeight) { this.clientPageHeight = clientPageHeight; }
    public List<RedactBoxDto> getBoxes() { return boxes; }
    public void setBoxes(List<RedactBoxDto> boxes) { this.boxes = boxes; }
}
