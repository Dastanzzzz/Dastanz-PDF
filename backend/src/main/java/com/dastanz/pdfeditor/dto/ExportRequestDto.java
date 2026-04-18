package com.dastanz.pdfeditor.dto;

import com.dastanz.pdfeditor.model.EditOperation;
import java.util.List;

public class ExportRequestDto {
    private String documentId;
    private List<EditOperation> edits;

    public ExportRequestDto() {
    }

    public ExportRequestDto(String documentId, List<EditOperation> edits) {
        this.documentId = documentId;
        this.edits = edits;
    }

    public String getDocumentId() { return documentId; }
    public List<EditOperation> getEdits() { return edits; }

    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setEdits(List<EditOperation> edits) { this.edits = edits; }
}
