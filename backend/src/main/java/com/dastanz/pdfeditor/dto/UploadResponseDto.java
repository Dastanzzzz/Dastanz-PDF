package com.dastanz.pdfeditor.dto;

import com.dastanz.pdfeditor.model.PageTextBlock;
import java.util.List;

public class UploadResponseDto {
    private String documentId;
    private List<PageTextBlock> textBlocks;

    public UploadResponseDto() {
    }

    public UploadResponseDto(String documentId, List<PageTextBlock> textBlocks) {
        this.documentId = documentId;
        this.textBlocks = textBlocks;
    }

    public String getDocumentId() { return documentId; }
    public List<PageTextBlock> getTextBlocks() { return textBlocks; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setTextBlocks(List<PageTextBlock> textBlocks) { this.textBlocks = textBlocks; }
}
