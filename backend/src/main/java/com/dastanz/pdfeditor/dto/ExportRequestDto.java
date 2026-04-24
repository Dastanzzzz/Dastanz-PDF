package com.dastanz.pdfeditor.dto;

import com.dastanz.pdfeditor.model.EditOperation;
import java.util.List;
import java.util.Map;

public class ExportRequestDto {
    private String documentId;
    private List<EditOperation> edits;
    private Map<String, Object> toolState;

    public ExportRequestDto() {
    }

    public ExportRequestDto(String documentId, List<EditOperation> edits, Map<String, Object> toolState) {
        this.documentId = documentId;
        this.edits = edits;
        this.toolState = toolState;
    }

    public String getDocumentId() { return documentId; }
    public List<EditOperation> getEdits() { return edits; }
    public Map<String, Object> getToolState() { return toolState; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setEdits(List<EditOperation> edits) { this.edits = edits; }
    public void setToolState(Map<String, Object> toolState) { this.toolState = toolState; }
}
